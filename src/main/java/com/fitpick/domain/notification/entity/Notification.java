package com.fitpick.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "try_on_id")
    private Long tryOnId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String body;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT(1)")
    @Builder.Default
    private Boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification create(Long userId, Long orderId,
                                      String title, String body,
                                      NotificationType type) {
        return Notification.builder()
                .userId(userId)
                .orderId(orderId)
                .title(title)
                .body(body)
                .notificationType(type)
                .build();
    }

    public static Notification createForTryOn(Long userId, Long tryOnId,
                                              String title, String body,
                                              String imageUrl,
                                              NotificationType type) {
        return Notification.builder()
                .userId(userId)
                .tryOnId(tryOnId)
                .title(title)
                .body(body)
                .imageUrl(imageUrl)
                .notificationType(type)
                .build();
    }

    // 읽음 처리 (나중에 알림 조회/읽음 API 붙일 때 사용)
    public void markAsRead() {
        this.isRead = true;
    }
}
