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
                description = "요청 data를 echo + notificationId(저장된 알림 ID, 클라이언트 read 처리용) 포함. "
                        + "FCM 메시지의 data 페이로드에는 notificationId가 들어가지 않고 요청 data 그대로 전송됨.",
                example = "{\"type\":\"PICKUP_READY\",\"orderId\":\"42\",\"notificationId\":\"57\"}"
        )
        Map<String, String> data
) {
}
