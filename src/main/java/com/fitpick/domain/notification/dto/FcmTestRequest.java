package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Schema(description = "[TEMP] FCM 테스트 발송 요청 — 3단계 통합 검증용. 시연 전 제거 예정.")
public record FcmTestRequest(

        @Schema(description = "알림 제목", example = "테스트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "알림 본문", example = "FCM 테스트입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 500) String body,

        @Schema(
                description = "선택적 데이터 페이로드. FCM 규격상 모든 값은 string으로 전송됨 "
                        + "(number/boolean 입력 시 String.valueOf로 자동 변환). 비우거나 생략 가능.",
                example = "{\"type\":\"PICKUP_READY\",\"orderId\":\"42\",\"deeplink\":\"fitpick://orders/42\"}"
        )
        Map<String, Object> data
) {
}
