package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.OrderItem;

public record OrderItemResponse(
        Long orderItemId,
        Long clothesOptionId,
        String clothesTitle,
        String size,
        String color,
        Integer quantity,
        Integer price
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getClothesOption().getId(),
                item.getClothesOption().getClothes().getTitle(),
                item.getClothesOption().getSize(),
                item.getClothesOption().getColor(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
