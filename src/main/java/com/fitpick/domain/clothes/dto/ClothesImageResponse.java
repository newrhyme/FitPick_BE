package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.ClothesImage;

public record ClothesImageResponse(
        Long imageId,
        String imageUrl,
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
