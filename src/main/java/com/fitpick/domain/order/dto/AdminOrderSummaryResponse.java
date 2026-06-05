package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.Order;

import java.time.LocalDateTime;

public record AdminOrderSummaryResponse(
        Long orderId,
        Long userId,            // 관리자는 누구 주문인지 봐야 함
        String orderType,
        String status,
        String statusDescription,
        Integer totalPrice,
        Integer itemCount,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public static AdminOrderSummaryResponse from(Order order) {
        return new AdminOrderSummaryResponse(
                order.getId(),
                order.getUserId(),
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
