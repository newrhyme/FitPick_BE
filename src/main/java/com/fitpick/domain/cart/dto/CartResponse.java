package com.fitpick.domain.cart.dto;

import com.fitpick.domain.cart.entity.Cart;
import com.fitpick.domain.cart.entity.CartItem;

import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        int totalQuantity,
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
