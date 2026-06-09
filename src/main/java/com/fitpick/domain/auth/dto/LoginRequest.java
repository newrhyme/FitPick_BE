package com.fitpick.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(

        @Schema(description = "로그인 아이디", example = "fitpick_user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String loginId,

        @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String password
) {
}
