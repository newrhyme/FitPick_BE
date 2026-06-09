package com.fitpick.domain.cart.dto;

import com.fitpick.domain.cart.entity.Cart;
import com.fitpick.domain.cart.entity.CartItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "장바구니 응답")
public record CartResponse(

        @Schema(description = "장바구니 ID (장바구니가 없으면 null)", example = "1")
        Long cartId,

        @Schema(description = "장바구니 항목 목록")
        List<CartItemResponse> items,

        @Schema(description = "전체 수량 합계", example = "3")
        int totalQuantity,

        @Schema(description = "전체 금액 합계 (원)", example = "117000")
        int totalPrice
) {
    public static CartResponse of(Cart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(CartItemResponse::from)
                .toList();

        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();
        int totalPrice = itemResponses.stream()
                .mapToInt(CartItemResponse::subtotal)
                .sum();

        return new CartResponse(cart.getId(), itemResponses, totalQuantity, totalPrice);
    }

    public static CartResponse empty() {
        return new CartResponse(null, List.of(), 0, 0);
    }
}
