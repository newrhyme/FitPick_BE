package com.fitpick.domain.notification.dto;

import com.fitpick.domain.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long notificationId,
        Long orderId,
        String title,
        String body,
        String notificationType,
        Boolean isRead,
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
