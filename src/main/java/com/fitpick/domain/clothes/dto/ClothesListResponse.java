package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;

public record ClothesListResponse(
        Long clothesId,
        String title,
        ClothesCategory category,
        Integer price,
        String thumbnailImageUrl,
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
