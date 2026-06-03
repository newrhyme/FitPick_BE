package com.fitpick.domain.clothes.service;

import com.fitpick.domain.clothes.dto.ClothesDetailResponse;
import com.fitpick.domain.clothes.dto.ClothesListResponse;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ClothesService {

    // 상품 목록 (카테고리 필터 + 페이징)
    PageResponse<ClothesListResponse> getClothesList(ClothesCategory category, Pageable pageable);

    // 상품 상세 조회
    ClothesDetailResponse getClothesDetail(Long clothesId, Long userId);
}
