package com.fitpick.domain.tryon.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesImage;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.exception.ClothesErrorCode;
import com.fitpick.domain.clothes.repository.ClothesImageRepository;
import com.fitpick.domain.clothes.repository.ClothesOptionRepository;
import com.fitpick.domain.clothes.repository.ClothesRepository;
import com.fitpick.domain.tryon.dto.TryOnCreateRequest;
import com.fitpick.domain.tryon.dto.TryOnListItemResponse;
import com.fitpick.domain.tryon.dto.TryOnResponse;
import com.fitpick.domain.tryon.entity.TryOn;
import com.fitpick.domain.tryon.entity.TryOnItem;
import com.fitpick.domain.tryon.exception.TryOnErrorCode;
import com.fitpick.domain.tryon.repository.TryOnRepository;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.exception.CustomException;
import com.fitpick.global.infra.openai.ImageDownloader;
import com.fitpick.global.infra.openai.ImageInput;
import com.fitpick.global.infra.openai.OpenAiImageClient;
import com.fitpick.global.infra.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TryOnServiceImpl implements TryOnService {

    private final UserRepository userRepository;
    private final ClothesRepository clothesRepository;
    private final ClothesOptionRepository clothesOptionRepository;
    private final ClothesImageRepository clothesImageRepository;
    private final TryOnRepository tryOnRepository;
    private final TryOnPersistenceService persistenceService;
    private final OpenAiImageClient openAiImageClient;
    private final ImageDownloader imageDownloader;
    private final S3Uploader s3Uploader;

    @Override
    public TryOnResponse create(Long userId, TryOnCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        String userImageUrl = user.getTryOnImageUrl();
        if (userImageUrl == null || userImageUrl.isBlank()) {
            throw new CustomException(TryOnErrorCode.NO_TRY_ON_IMAGE);
        }

        Clothes clothes = clothesRepository.findById(request.clothesId())
                .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND));
        ClothesOption option = clothesOptionRepository.findById(request.clothesOptionId())
                .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_OPTION_NOT_FOUND));
        if (!Objects.equals(option.getClothes().getId(), clothes.getId())) {
            throw new CustomException(TryOnErrorCode.OPTION_NOT_BELONG_TO_CLOTHES);
        }

        String productImageUrl = resolveProductImageUrl(clothes);
        if (productImageUrl == null || productImageUrl.isBlank()) {
            throw new CustomException(TryOnErrorCode.NO_PRODUCT_IMAGE);
        }

        // PROCESSING 저장 — REQUIRES_NEW로 즉시 커밋 (외부 가시성)
        TryOn tryOn = persistenceService.createProcessing(userId, userImageUrl, productImageUrl, option.getId());
        Long tryOnId = tryOn.getId();

        try {
            // 외부 호출 구간 — 트랜잭션 밖, DB 커넥션 점유 없음
            ImageInput personImage = imageDownloader.download(userImageUrl, "person.png");
            ImageInput productImage = imageDownloader.download(productImageUrl, "product.png");
            String englishColor = toEnglishColor(option.getColor());
            String categoryName = clothes.getCategory() != null ? clothes.getCategory().name() : null;
            String prompt = buildPrompt(englishColor, categoryName);
            byte[] generated = openAiImageClient.editImage(prompt, personImage, productImage);

            // S3 업로드 — OpenAI 성공 시에만 (실패 시 비용 회피)
            String s3Key = "users/" + userId + "/try-ons/" + tryOnId + "/result.png";
            String generatedUrl = s3Uploader.uploadBytes(generated, s3Key, "image/png");

            // DONE 저장 — REQUIRES_NEW
            TryOn done = persistenceService.markDone(tryOnId, generatedUrl);
            return TryOnResponse.of(done, clothes.getId(), option.getId());

        } catch (Exception e) {
            log.error("가상 피팅 생성 실패. tryOnId={}, userId={}", tryOnId, userId, e);
            String reason = buildFailureReason(e);
            try {
                persistenceService.markFailed(tryOnId, reason);
            } catch (Exception inner) {
                log.error("FAILED 저장도 실패. tryOnId={}", tryOnId, inner);
            }
            throw new CustomException(TryOnErrorCode.GENERATION_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TryOnResponse get(Long userId, Long tryOnId) {
        TryOn tryOn = tryOnRepository.findByIdWithItems(tryOnId)
                .orElseThrow(() -> new CustomException(TryOnErrorCode.TRY_ON_NOT_FOUND));
        if (!Objects.equals(tryOn.getUserId(), userId)) {
            throw new CustomException(TryOnErrorCode.TRY_ON_ACCESS_DENIED);
        }

        Long clothesOptionId = firstClothesOptionId(tryOn);
        Long clothesId = clothesOptionId == null
                ? null
                : clothesOptionRepository.findById(clothesOptionId)
                        .map(o -> o.getClothes().getId())
                        .orElse(null);
        return TryOnResponse.of(tryOn, clothesId, clothesOptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TryOnListItemResponse> getMyList(Long userId) {
        List<TryOn> tryOns = tryOnRepository.findAllByUserIdWithItemsOrderByCreatedAtDesc(userId);
        if (tryOns.isEmpty()) {
            return List.of();
        }

        // tryOnId -> 첫 옵션ID 매핑 (1:1 사용 정책)
        Map<Long, Long> tryOnIdToOptionId = new LinkedHashMap<>();
        for (TryOn t : tryOns) {
            tryOnIdToOptionId.put(t.getId(), firstClothesOptionId(t));
        }

        // 옵션ID 모음 → 옵션 + 옷 한 번에 fetch (N+1 회피)
        Set<Long> optionIds = tryOnIdToOptionId.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ClothesOption> optionMap = optionIds.isEmpty()
                ? Map.of()
                : clothesOptionRepository.findAllByIdInWithClothes(optionIds).stream()
                        .collect(Collectors.toMap(ClothesOption::getId, Function.identity()));

        return tryOns.stream().map(t -> {
            Long optId = tryOnIdToOptionId.get(t.getId());
            ClothesOption opt = optId != null ? optionMap.get(optId) : null;
            Clothes c = opt != null ? opt.getClothes() : null;
            return new TryOnListItemResponse(
                    t.getId(),
                    c != null ? c.getId() : null,
                    c != null ? c.getTitle() : null,
                    optId,
                    t.getStatus().name(),
                    t.getOriginalImageUrl(),
                    t.getProductImageUrl(),
                    t.getGeneratedImageUrl(),
                    t.getCreatedAt()
            );
        }).toList();
    }

    private Long firstClothesOptionId(TryOn tryOn) {
        List<TryOnItem> items = tryOn.getItems();
        return items.isEmpty() ? null : items.get(0).getClothesOptionId();
    }

    private String resolveProductImageUrl(Clothes clothes) {
        List<ClothesImage> images = clothesImageRepository.findByClothesIdOrderBySortOrderAsc(clothes.getId());
        if (!images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return clothes.getThumbnailImageUrl();
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
