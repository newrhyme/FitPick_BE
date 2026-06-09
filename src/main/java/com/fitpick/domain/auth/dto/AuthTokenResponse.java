package com.fitpick.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record AuthTokenResponse(

        @Schema(description = "JWT access token — Authorization: Bearer {accessToken} 헤더에 담아 사용",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
