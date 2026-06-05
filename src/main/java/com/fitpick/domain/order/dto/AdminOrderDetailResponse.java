package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderDetailResponse(
        Long orderId,

        // 주문자 정보
        Long userId,
        String userLoginId,
        String userName,

        // 주문 정보
        String orderType,
        String status,
        String statusDescription,
        Integer totalPrice,
        LocalDateTime paidAt,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static AdminOrderDetailResponse of(Order order, User user) {
        return new AdminOrderDetailResponse(
                order.getId(),
                user.getId(),
                user.getLoginId(),
                user.getName(),
                order.getOrderType().name(),
                order.getStatus().name(),
                order.getStatus().getDescription(),
                order.getTotalPrice(),
                order.getPaidAt(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}
