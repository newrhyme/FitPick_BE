package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.entity.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 옵션")
public record ClothesOptionResponse(

        @Schema(description = "옵션 ID", example = "1")
        Long optionId,

        @Schema(description = "사이즈", example = "M")
        String size,

        @Schema(description = "색상", example = "화이트")
        String color,

        @Schema(description = "재고 수량", example = "10")
        Integer stockQuantity,

        @Schema(description = "재고 상태 (AVAILABLE / LOW_STOCK / SOLD_OUT)", example = "AVAILABLE")
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
