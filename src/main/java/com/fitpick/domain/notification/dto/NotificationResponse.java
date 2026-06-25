package com.fitpick.domain.notification.dto;

import com.fitpick.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 항목")
public record NotificationResponse(

        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "연관 주문 ID (notificationType=ORDER 인 경우, 그 외 null)", example = "10")
        Long orderId,

        @Schema(description = "연관 가상 피팅 ID (notificationType=TRYON 인 경우, 그 외 null)", example = "5")
        Long tryOnId,

        @Schema(description = "알림 제목", example = "#10")
        String title,

        @Schema(description = "알림 본문", example = "주문하신 상품이 픽업 준비되었습니다.")
        String body,

        @Schema(description = "알림 첨부 이미지 URL (TRYON 이면 generatedImageUrl)", example = "https://.../result.png")
        String imageUrl,

        @Schema(description = "알림 유형 (ORDER | TRYON)", example = "ORDER")
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
                n.getTryOnId(),
                n.getTitle(),
                n.getBody(),
                n.getImageUrl(),
                n.getNotificationType().name(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }
}
