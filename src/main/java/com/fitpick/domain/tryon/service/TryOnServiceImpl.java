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
            String prompt = buildPrompt(clothes, option);
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

    private String buildPrompt(Clothes clothes, ClothesOption option) {
        return """
                Create a realistic virtual try-on image for a shopping app.
                Use the first image as the person's full-body reference.
                Use the second image as the clothing or accessory product reference.
                Dress the person in the product naturally.
                Preserve the person's identity, face, body shape, pose, background, and lighting as much as possible.
                Do not change the person's face or identity.
                Do not add extra products.
                Make the output look like a realistic product fitting preview.
                If the product is an accessory, apply the accessory to the person naturally while preserving the original outfit.

                Product title: %s
                Category: %s
                Selected color: %s
                Selected size: %s
                """.formatted(
                clothes.getTitle() != null ? clothes.getTitle() : "",
                clothes.getCategory() != null ? clothes.getCategory().name() : "",
                option.getColor() != null ? option.getColor() : "",
                option.getSize() != null ? option.getSize() : ""
        );
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
