package com.fitpick.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "[TEMP] FCM 테스트 발송 요청 — 3단계 통합 검증용. 시연 전 제거 예정.")
public record FcmTestRequest(

        @Schema(description = "알림 제목", example = "테스트", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 100) String title,

        @Schema(description = "알림 본문", example = "FCM 테스트입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 500) String body
) {
}
