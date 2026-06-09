package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.ClothesImage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 이미지")
public record ClothesImageResponse(

        @Schema(description = "이미지 ID", example = "1")
        Long imageId,

        @Schema(description = "이미지 URL", example = "https://example.com/img/shirt_1.jpg")
        String imageUrl,

        @Schema(description = "정렬 순서 (오름차순)", example = "0")
        Integer sortOrder
) {
    public static ClothesImageResponse from(ClothesImage image) {
        return new ClothesImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getSortOrder()
        );
    }
}
