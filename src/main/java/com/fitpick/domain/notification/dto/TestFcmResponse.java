package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "[TEMP] FCM 테스트 발송 응답")
public record TestFcmResponse(

        @Schema(description = "저장된 알림 ID — 클라이언트가 read 처리 호출 시 사용", example = "57")
        Long notificationId,

        @Schema(description = "FCM 발송 성공 여부", example = "true")
        boolean sent,

        @Schema(description = "FCM 응답 messageId (sent=true일 때만)")
        String messageId,

        @Schema(description = "발송 실패/skip 사유 (sent=false일 때, 예: FCM_DISABLED / TOKEN_EMPTY / 오류 메시지)")
        String reason
) {
}
