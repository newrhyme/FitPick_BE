package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안 읽은 알림 개수 응답 (레드닷 표시용)")
public record UnreadCountResponse(

        @Schema(description = "본인의 isRead=false 알림 개수", example = "3")
        long unreadCount
) {
}
