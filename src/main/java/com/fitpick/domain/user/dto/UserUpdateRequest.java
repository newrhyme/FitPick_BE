package com.fitpick.domain.user.dto;

import com.fitpick.domain.user.entity.AgeGroup;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 정보 수정 요청 (부분 수정 — null/누락 필드는 변경하지 않음)")
public record UserUpdateRequest(

        @Schema(description = "전화번호", example = "010-2222-3333")
        String phone,

        @Schema(description = "키 (cm)", example = "170")
        Integer height,

        @Schema(description = "몸무게 (kg)", example = "65")
        Integer weight,

        @Schema(description = "연령대 (TEENS / TWENTIES / THIRTIES / FORTIES_PLUS)", example = "TWENTIES")
        AgeGroup ageGroup,

        @Schema(description = "주소", example = "서울 강남구 ...")
        String address
) {
}
