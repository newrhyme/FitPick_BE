package com.fitpick.domain.notification.dto;

import com.fitpick.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 항목")
public record NotificationResponse(

        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "연관 주문 ID (주문 알림인 경우)", example = "10")
        Long orderId,

        @Schema(description = "알림 제목", example = "픽업 준비 완료")
        String title,

        @Schema(description = "알림 본문", example = "주문하신 상품이 픽업 준비되었습니다.")
        String body,

        @Schema(description = "알림 유형 (PICKUP_READY 등)", example = "PICKUP_READY")
        String notificationType,

        @Schema(description = "읽음 여부", example = "false")
        Boolean isRead,

        @Schema(description = "알림 생성 시각", example = "2026-06-05T11:00:00")
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getOrderId(),
                n.getTitle(),
                n.getBody(),
                n.getNotificationType().name(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
