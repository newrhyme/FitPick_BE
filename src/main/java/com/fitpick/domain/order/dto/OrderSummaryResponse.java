package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "주문 목록 항목 (요약)")
public record OrderSummaryResponse(

        @Schema(description = "주문 ID", example = "10")
        Long orderId,

        @Schema(description = "주문 유형 (CART / DIRECT)", example = "CART")
        String orderType,

        @Schema(description = "주문 상태", example = "PAID")
        String status,

        @Schema(description = "주문 상태 설명", example = "결제 완료")
        String statusDescription,

        @Schema(description = "총 결제 금액 (원)", example = "78000")
        Integer totalPrice,

        @Schema(description = "주문 항목 수", example = "2")
        Integer itemCount,

        @Schema(description = "결제 완료 시각", example = "2026-06-05T10:15:30")
        LocalDateTime paidAt,

        @Schema(description = "주문 생성 시각", example = "2026-06-05T10:15:29")
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
