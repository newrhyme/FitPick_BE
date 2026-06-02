package com.fitpick.domain.clothes.service;

import com.fitpick.domain.clothes.dto.ClothesDetailResponse;
import com.fitpick.domain.clothes.dto.ClothesListResponse;
import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.domain.clothes.entity.ClothesImage;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.exception.ClothesErrorCode;
import com.fitpick.domain.clothes.repository.ClothesImageRepository;
import com.fitpick.domain.clothes.repository.ClothesOptionRepository;
import com.fitpick.domain.clothes.repository.ClothesRepository;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService {

    private final ClothesRepository clothesRepository;
    private final ClothesOptionRepository clothesOptionRepository;
    private final ClothesImageRepository clothesImageRepository;

    @Override
    public PageResponse<ClothesListResponse> getClothesList(ClothesCategory category, Pageable pageable) {
        Page<Clothes> clothesPage = (category == null)
                ? clothesRepository.findByIsActiveTrue(pageable)
                : clothesRepository.findByIsActiveTrueAndCategory(category, pageable);

        // batch size(100) 덕에 options 로딩이 IN 쿼리로 묶임 — N+1 없음
        Page<ClothesListResponse> responsePage = clothesPage.map(clothes -> {
            boolean soldOut = clothes.getOptions().stream()
                    .allMatch(opt -> opt.getStockQuantity() == 0);
            return ClothesListResponse.from(clothes, soldOut);
        });

        return PageResponse.from(responsePage);
    }

    @Override
    public ClothesDetailResponse getClothesDetail(Long clothesId) {
        // 단건 fetch join — 메모리 페이징 문제 없음
        Clothes clothes = clothesRepository.findByIdWithOptions(clothesId)
                .orElseThrow(() -> new CustomException(ClothesErrorCode.CLOTHES_NOT_FOUND));
        List<ClothesOption> options = clothes.getOptions();

        List<ClothesImage> images =
                clothesImageRepository.findByClothesIdOrderBySortOrderAsc(clothesId);

        return ClothesDetailResponse.of(clothes, options, images);
    }
}
