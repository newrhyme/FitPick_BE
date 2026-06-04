package com.fitpick.domain.cart.dto;

import com.fitpick.domain.cart.entity.CartItem;
import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.domain.clothes.entity.StockStatus;

public record CartItemResponse(
        Long cartItemId,
        Long clothesId,
        Long optionId,
        String title,           // 상품 명
        String thumbnailImageUrl,
        String size,
        String color,
        Integer price,          // 상품 단가
        Integer quantity,       // 담은 수량
        Integer subtotal,       // 소계
        StockStatus stockStatus // 재고 상태
) {
    public static CartItemResponse from(CartItem item) {
        ClothesOption option= item.getClothesOption();
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
