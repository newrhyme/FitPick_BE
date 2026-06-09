package com.fitpick.domain.cart.dto;

import com.fitpick.domain.cart.entity.CartItem;
import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.entity.StockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 항목")
public record CartItemResponse(

        @Schema(description = "장바구니 항목 ID", example = "5")
        Long cartItemId,

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "선택한 옵션 ID", example = "2")
        Long optionId,

        @Schema(description = "상품명", example = "오버핏 코튼 셔츠")
        String title,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/img/shirt_thumb.jpg")
        String thumbnailImageUrl,

        @Schema(description = "사이즈", example = "M")
        String size,

        @Schema(description = "색상", example = "화이트")
        String color,

        @Schema(description = "상품 단가 (원)", example = "39000")
        Integer price,

        @Schema(description = "담은 수량", example = "2")
        Integer quantity,

        @Schema(description = "소계 = 단가 × 수량 (원)", example = "78000")
        Integer subtotal,

        @Schema(description = "재고 상태 (AVAILABLE / LOW_STOCK / SOLD_OUT)", example = "AVAILABLE")
        StockStatus stockStatus
) {
    public static CartItemResponse from(CartItem item) {
        ClothesOption option = item.getClothesOption();
        Clothes clothes = option.getClothes();
        int price = clothes.getPrice();
        int quantity = item.getQuantity();

        return new CartItemResponse(
                item.getId(),
                clothes.getId(),
                option.getId(),
                clothes.getTitle(),
                clothes.getThumbnailImageUrl(),
                option.getSize(),
                option.getColor(),
                price,
                quantity,
                price * quantity,
                StockStatus.of(option.getStockQuantity())
        );
    }
}
