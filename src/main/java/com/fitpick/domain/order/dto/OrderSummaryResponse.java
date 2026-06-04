package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.Order;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long orderId,
        String orderType,
        String status,
        String statusDescription,
        Integer totalPrice,
        Integer itemCount,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderType().name(),
                order.getStatus().name(),
                order.getStatus().getDescription(),
                order.getTotalPrice(),
                order.getItems().size(),
                order.getPaidAt(),
                order.getCreatedAt()
        );
    }
}
