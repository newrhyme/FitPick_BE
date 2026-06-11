package com.fitpick.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(

        @Schema(description = "JWT access token — Authorization: Bearer {accessToken} 헤더에 담아 사용",
                example = "eyJhbGciOiJIUzM4NCJ9...")
        String accessToken,

        @Schema(description = "로그인한 사용자 정보")
        LoginUserResponse user
) {
}
