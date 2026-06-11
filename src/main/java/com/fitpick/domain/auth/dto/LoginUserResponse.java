package com.fitpick.domain.auth.dto;

import com.fitpick.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인한 사용자 정보")
public record LoginUserResponse(

        @Schema(description = "사용자 ID", example = "6")
        Long userId,

        @Schema(description = "로그인 아이디", example = "customer01")
        String loginId,

        @Schema(description = "이름", example = "김지민")
        String name,

        @Schema(description = "역할 (CUSTOMER / STAFF / ADMIN)", example = "CUSTOMER")
        String role,

        @Schema(description = "전화번호", example = "010-1000-0001")
        String phone,

        @Schema(description = "키 (cm)", example = "165")
        Integer height,

        @Schema(description = "몸무게 (kg)", example = "52")
        Integer weight,

        @Schema(description = "연령대 (TEENS / TWENTIES / THIRTIES / FORTIES_PLUS)", example = "TWENTIES")
        String ageGroup,

        @Schema(description = "주소", example = "서울시 강남구")
        String address,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl
) {
    public static LoginUserResponse from(User user) {
        return new LoginUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getRole().name(),
                user.getPhone(),
                user.getHeight(),
                user.getWeight(),
                user.getAgeGroup() != null ? user.getAgeGroup().name() : null,
                user.getAddress(),
                user.getProfileImageUrl()
        );
    }
}
