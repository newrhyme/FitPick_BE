package com.fitpick.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(

        @Schema(description = "로그인 아이디 (최대 50자)", example = "fitpick_user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 50) String loginId,

        @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String password,

        @Schema(description = "이름 (최대 50자)", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 50) String name,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "키 (cm)", example = "175")
        Integer height,

        @Schema(description = "몸무게 (kg)", example = "70")
        Integer weight,

        @Schema(description = "연령대", example = "20대")
        String ageGroup,

        @Schema(description = "주소", example = "서울시 강남구")
        String address
) {
}
