package com.fitpick.domain.clothes.controller.docs;

import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Clothes", description = "상품 조회 API")
public interface ClothesApiDocs {

    @Operation(
            summary = "상품 리스트 조회",
            description = "활성 상품 목록을 최신순으로 조회합니다. category 파라미터로 필터링, page/size로 페이징 가능합니다."
    )
    ApiResponse<?> getClothesList(
            @Parameter(description = "카테고리 필터 (TOP/BOTTOM/OUTER/DRESS/SHOES/BAG/ACCESSORY). 생략 시 전체 조회.")
            ClothesCategory category,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID로 상세 정보(기본 정보, 옵션, 이미지)를 조회합니다. 옵션의 재고는 상태(AVAILABLE/SOLD_OUT)로만 노출됩니다."
    )
    ApiResponse<?> getClothesDetail(Long clothesId);
}
