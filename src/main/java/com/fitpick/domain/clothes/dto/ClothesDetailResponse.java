package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.domain.clothes.entity.ClothesImage;
import com.fitpick.domain.clothes.entity.ClothesOption;

import java.util.List;

public record ClothesDetailResponse(
        Long clothesId,
        String title,
        String description,
        ClothesCategory category,
        String material,
        Integer price,
        String thumbnailImageUrl,
        List<ClothesOptionResponse> options,
        List<ClothesImageResponse> images
) {
    public static ClothesDetailResponse of(
            Clothes clothes,
            List<ClothesOption> options,
            List<ClothesImage> images
    ) {
        return new ClothesDetailResponse(
                clothes.getId(),
                clothes.getTitle(),
                clothes.getDescription(),
                clothes.getCategory(),
                clothes.getMaterial(),
                clothes.getPrice(),
                clothes.getThumbnailImageUrl(),
                options.stream().map(ClothesOptionResponse::from).toList(),
                images.stream().map(ClothesImageResponse::from).toList()
        );
    }
}
