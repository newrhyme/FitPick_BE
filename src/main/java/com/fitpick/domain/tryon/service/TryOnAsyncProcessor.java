package com.fitpick.domain.tryon.service;

import com.fitpick.domain.notification.service.NotificationService;
import com.fitpick.domain.tryon.entity.TryOn;
import com.fitpick.global.config.AsyncConfig;
import com.fitpick.global.infra.openai.ImageDownloader;
import com.fitpick.global.infra.openai.ImageInput;
import com.fitpick.global.infra.openai.OpenAiImageClient;
import com.fitpick.global.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TryOnAsyncProcessor {

    private final TryOnPersistenceService persistenceService;
    private final NotificationService notificationService;
    private final OpenAiImageClient openAiImageClient;
    private final ImageDownloader imageDownloader;
    private final S3Uploader s3Uploader;

    // OpenAI 호출 + S3 업로드 + DB 상태 업데이트 + 알림 발송 — 전용 스레드풀(tryOnExecutor)에서 비동기 실행.
    // 호출 직전에 PROCESSING 행이 이미 커밋돼 있어야 함 (TryOnServiceImpl.create에서 보장).
    @Async(AsyncConfig.TRY_ON_EXECUTOR)
    public void process(Long tryOnId,
                        Long userId,
                        String userImageUrl,
                        String productImageUrl,
                        String color,
                        String category) {
        try {
            ImageInput personImage = imageDownloader.download(userImageUrl, "person.png");
            ImageInput productImage = imageDownloader.download(productImageUrl, "product.png");
            String englishColor = toEnglishColor(color);
            String prompt = buildPrompt(englishColor, category);
            byte[] generated = openAiImageClient.editImage(prompt, personImage, productImage);

            String s3Key = "users/" + userId + "/try-ons/" + tryOnId + "/result.png";
            String generatedUrl = s3Uploader.uploadBytes(generated, s3Key, "image/png");

            TryOn done = persistenceService.markDone(tryOnId, generatedUrl);

            try {
                notificationService.notifyTryOnDone(done);
            } catch (Exception notifyEx) {
                log.error("가상 피팅 완료 알림 발송 실패. tryOnId={}", tryOnId, notifyEx);
            }
        } catch (Exception e) {
            log.error("비동기 가상 피팅 처리 실패. tryOnId={}, userId={}", tryOnId, userId, e);
            String reason = buildFailureReason(e);
            try {
                persistenceService.markFailed(tryOnId, reason);
            } catch (Exception inner) {
                log.error("FAILED 저장도 실패. tryOnId={}", tryOnId, inner);
            }
            try {
                notificationService.notifyTryOnFailed(userId, tryOnId);
            } catch (Exception notifyEx) {
                log.error("가상 피팅 실패 알림 발송 실패. tryOnId={}", tryOnId, notifyEx);
            }
        }
    }

    private static final Map<String, String> COLOR_KO_TO_EN = Map.ofEntries(
            Map.entry("화이트", "white"),
            Map.entry("블랙", "black"),
            Map.entry("그레이", "gray"),
            Map.entry("회색", "gray"),
            Map.entry("네이비", "navy"),
            Map.entry("블루", "blue"),
            Map.entry("파랑", "blue"),
            Map.entry("레드", "red"),
            Map.entry("빨강", "red"),
            Map.entry("핑크", "pink"),
            Map.entry("분홍", "pink"),
            Map.entry("베이지", "beige"),
            Map.entry("브라운", "brown"),
            Map.entry("갈색", "brown"),
            Map.entry("카키", "khaki"),
            Map.entry("그린", "green"),
            Map.entry("초록", "green"),
            Map.entry("옐로우", "yellow"),
            Map.entry("노랑", "yellow"),
            Map.entry("오렌지", "orange"),
            Map.entry("주황", "orange"),
            Map.entry("퍼플", "purple"),
            Map.entry("보라", "purple"),
            Map.entry("아이보리", "ivory"),
            Map.entry("민트", "mint"),
            Map.entry("스카이블루", "sky blue"),
            Map.entry("와인", "wine")
    );

    private String toEnglishColor(String color) {
        if (color == null || color.isBlank()) {
            return null;
        }
        String trimmed = color.trim();
        if (trimmed.matches("[A-Za-z ]+")) {
            return trimmed.toLowerCase();
        }
        return COLOR_KO_TO_EN.get(trimmed);
    }

    private String buildPrompt(String color, String category) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                    Create a realistic virtual try-on image for a shopping app.

                    Use the first image as the person's full-body reference.
                    Use the second image as the clothing or accessory product reference.
                    Dress or equip the person with the product naturally.

                    CRITICAL RULES — must not violate:
                    - DO NOT change the person's face. The face must be IDENTICAL to the first image: same facial features, same skin tone, same hair, same eyes, same nose, same mouth, same expression.
                    - DO NOT add any text, labels, captions, watermarks, logos, or written characters to the output image.
                    - DO NOT overlay product information on the image.
                    - The output must be a clean photograph with no annotations.

                    Face preservation (highest priority):
                    - The person in the output MUST be recognizable as the same individual from the first image.
                    - Treat the face region as a locked area — do not regenerate, redraw, or modify it.
                    - Keep all facial proportions, skin texture, and identifying features exactly as in the source.

                    Body and scene preservation:
                    - Preserve the person's body shape, pose, hands, and posture.
                    - Preserve the background and lighting exactly.
                    - Do not change the person's hair style or color.
                    - Do not add or remove other clothing items unless replacing the same category.
                    - If the product is an accessory (bag, hat, watch), apply it naturally while keeping the original outfit.

                    Reference image isolation (HIGHEST priority for product handling):
                    - The second image may show the target product worn together with OTHER clothing items
                      (top, bottom, outer, shoes, bag, hat, accessories) on a model.
                      These additional items are NOT the product — they are styling around the product.
                    - Extract ONLY the specified product category from the second image.
                    - Completely IGNORE every other garment, accessory, color, and styling element visible in
                      the second image. Do not transfer the model's top when the product is a bottom.
                      Do not transfer the model's bottom when the product is a top.
                      Do not transfer shoes, bags, or accessories that are not the product.
                    - The person's existing clothing in all non-target categories must remain EXACTLY as in
                      the first image (same garment, same color, same shape, same length).
                    - If the product is BOTTOM, the person's top in the output must be identical to the top
                      in the first image. If the product is TOP, the person's bottom in the output must be
                      identical to the bottom in the first image. Same rule for every category combination.

                    The output should look like the same photograph with only the relevant clothing/accessory changed.
                """);

        if (category != null && !category.isBlank()) {
            sb.append("Product category: ").append(category).append("\n\n");
            sb.append("""
                    Determine how to apply the product based on category:
                    - TOP / BOTTOM / OUTER / DRESS: replace the corresponding existing garment on the person.
                    - BAG / HAT / SCARF / WATCH / ACCESSORY: add the item naturally while preserving all existing clothing.
                    - SHOES: replace existing footwear.

                    """);
        }

        if (color != null && !color.isBlank()) {
            sb.append("Color reference (if applicable): Use a ")
                    .append(color)
                    .append(" colored variant of the product.\n\n");
        }

        sb.append("The output should match a clean studio photo style consistent with the reference images.\n");
        return sb.toString();
    }

    private String buildFailureReason(Exception e) {
        String type = e.getClass().getSimpleName();
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return type;
        }
        return type + ": " + msg;
    }
}
