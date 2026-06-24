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
    private final TryOnAsyncProcessor asyncProcessor;

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

        // style은 공백이면 null로 정규화
        String style = (request.style() != null && !request.style().isBlank()) ? request.style().trim() : null;

        // PROCESSING 저장 — REQUIRES_NEW로 즉시 커밋 (async 워커가 바로 조회 가능)
        TryOn tryOn = persistenceService.createProcessing(userId, userImageUrl, productImageUrl, option.getId(), style);

        // 백그라운드 처리 — OpenAI 호출/S3 업로드/DB DONE 업데이트/알림 발송
        String category = clothes.getCategory() != null ? clothes.getCategory().name() : null;
        asyncProcessor.process(tryOn.getId(), userId, userImageUrl, productImageUrl, option.getColor(), category, style);

        // 즉시 응답 — status는 PROCESSING, generatedImageUrl은 null
        return TryOnResponse.of(tryOn, clothes.getId(), option.getId(), option.getSize(), option.getColor());
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
        ClothesOption opt = clothesOptionId == null
                ? null
                : clothesOptionRepository.findById(clothesOptionId).orElse(null);
        Long clothesId = opt != null ? opt.getClothes().getId() : null;
        String size = opt != null ? opt.getSize() : null;
        String color = opt != null ? opt.getColor() : null;
        return TryOnResponse.of(tryOn, clothesId, clothesOptionId, size, color);
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
                    opt != null ? opt.getSize() : null,
                    opt != null ? opt.getColor() : null,
                    t.getStatus().name(),
                    t.getOriginalImageUrl(),
                    t.getProductImageUrl(),
                    t.getGeneratedImageUrl(),
                    t.getStyle(),
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
}
