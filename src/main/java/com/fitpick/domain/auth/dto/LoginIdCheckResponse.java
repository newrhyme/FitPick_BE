package com.fitpick.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 아이디 중복 체크 응답")
public record LoginIdCheckResponse(

        @Schema(description = "검사한 로그인 아이디", example = "customer01")
        String loginId,

        @Schema(description = "사용 가능 여부 (true=사용 가능, false=이미 사용 중)", example = "false")
        boolean available
) {
}
