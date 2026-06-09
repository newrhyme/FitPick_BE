package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 목록 항목")
public record ClothesListResponse(

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "상품명", example = "오버핏 코튼 셔츠")
        String title,

        @Schema(description = "카테고리", example = "TOP")
        ClothesCategory category,

        @Schema(description = "가격 (원)", example = "39000")
        Integer price,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/img/shirt_thumb.jpg")
        String thumbnailImageUrl,

        @Schema(description = "품절 여부 — 모든 옵션의 재고가 0이면 true", example = "false")
        boolean soldOut
) {
    public static ClothesListResponse from(Clothes clothes, boolean soldOut) {
        return new ClothesListResponse(
                clothes.getId(),
                clothes.getTitle(),
                clothes.getCategory(),
                clothes.getPrice(),
                clothes.getThumbnailImageUrl(),
                soldOut
        );
    }
}
