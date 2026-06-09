package com.fitpick.domain.clothes.controller.docs;

import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Clothes", description = "상품 조회 API")
public interface ClothesApiDocs {

    @Operation(
            summary = "상품 리스트 조회",
            description = "활성 상품 목록을 최신순으로 조회합니다. category 파라미터로 필터링, page/size로 페이징 가능합니다. JWT 없이 호출 가능합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — PageResponse<ClothesListResponse>")
    })
    ApiResponse<?> getClothesList(
            @Parameter(description = "카테고리 필터 (TOP/BOTTOM/OUTER/DRESS/SHOES/BAG/ACCESSORY). 생략 시 전체 조회.")
            ClothesCategory category,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "상품 ID로 상세 정보(기본 정보, 옵션, 이미지)를 조회합니다. JWT 없이 호출 가능하며, JWT가 있으면 조회 이력이 기록됩니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — ClothesDetailResponse"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 없음 (C001)")
    })
    ApiResponse<?> getClothesDetail(Long clothesId, @AuthenticationPrincipal CustomUserDetails userDetails);
}
