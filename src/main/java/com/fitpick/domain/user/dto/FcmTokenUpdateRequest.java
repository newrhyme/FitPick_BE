package com.fitpick.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "FCM 토큰 등록/갱신 요청")
public record FcmTokenUpdateRequest(

        @Schema(description = "FCM SDK가 발급한 디바이스 토큰", example = "ej3kf...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 500) String fcmToken
) {
}
