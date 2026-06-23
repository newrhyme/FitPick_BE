package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "[TEMP] FCM 테스트 발송 응답")
public record TestFcmResponse(

        @Schema(description = "FCM 발송 성공 여부", example = "true")
        boolean sent,

        @Schema(description = "FCM 응답 messageId (sent=true일 때만)")
        String messageId,

        @Schema(description = "발송 실패/skip 사유 (sent=false일 때, 예: FCM_DISABLED / TOKEN_EMPTY / 오류 메시지)")
        String reason,

        @Schema(
                description = "FCM 메시지의 data와 동일 — 요청 data + notificationId(서버 자동 주입). "
                        + "모든 값은 string (Firebase 명세). 클라이언트는 notificationId로 read 처리 호출.",
                example = "{\"type\":\"PICKUP_READY\",\"orderId\":\"42\",\"notificationId\":\"57\"}"
        )
        Map<String, String> data
) {
}
