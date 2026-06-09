package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자용 주문 상세 응답")
public record AdminOrderDetailResponse(

        @Schema(description = "주문 ID", example = "10")
        Long orderId,

        @Schema(description = "주문자 ID", example = "3")
        Long userId,

        @Schema(description = "주문자 로그인 아이디", example = "fitpick_user")
        String userLoginId,

        @Schema(description = "주문자 이름", example = "홍길동")
        String userName,

        @Schema(description = "주문 유형 (CART / DIRECT)", example = "CART")
        String orderType,

        @Schema(description = "주문 상태", example = "PREPARING")
        String status,

        @Schema(description = "주문 상태 설명", example = "준비 중")
        String statusDescription,

        @Schema(description = "총 결제 금액 (원)", example = "78000")
        Integer totalPrice,

        @Schema(description = "결제 완료 시각", example = "2026-06-05T10:15:30")
        LocalDateTime paidAt,

        @Schema(description = "주문 생성 시각", example = "2026-06-05T10:15:29")
        LocalDateTime createdAt,

        @Schema(description = "주문 항목 목록")
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
