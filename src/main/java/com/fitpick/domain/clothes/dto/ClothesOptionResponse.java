package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.entity.StockStatus;

public record ClothesOptionResponse(
        Long optionId,
        String size,
        String color,
        Integer stockQuantity,
        StockStatus stockStatus
) {
    public static ClothesOptionResponse from(ClothesOption option) {
        return new ClothesOptionResponse(
                option.getId(),
                option.getSize(),
                option.getColor(),
                option.getStockQuantity(),
                StockStatus.of(option.getStockQuantity())
        );
    }
}
