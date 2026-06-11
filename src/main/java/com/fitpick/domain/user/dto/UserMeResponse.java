package com.fitpick.domain.user.dto;

import com.fitpick.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 사용자 정보")
public record UserMeResponse(

        @Schema(description = "사용자 ID", example = "9")
        Long userId,

        @Schema(description = "로그인 아이디", example = "customer04")
        String loginId,

        @Schema(description = "이름", example = "최현우")
        String name,

        @Schema(description = "역할 (CUSTOMER / STAFF / ADMIN)", example = "CUSTOMER")
        String role,

        @Schema(description = "전화번호", example = "010-1000-0004")
        String phone,

        @Schema(description = "키 (cm)", example = "182")
        Integer height,

        @Schema(description = "몸무게 (kg)", example = "78")
        Integer weight,

        @Schema(description = "연령대 (TEENS / TWENTIES / THIRTIES / FORTIES_PLUS)", example = "THIRTIES")
        String ageGroup,

        @Schema(description = "주소")
        String address,

        @Schema(description = "프로필 이미지 URL")
        String profileImageUrl,

        @Schema(description = "가상 착용 이미지 URL")
        String tryOnImageUrl,

        @Schema(description = "가상 착용 이미지 존재 여부", example = "true")
        boolean hasTryOnImage,

        @Schema(description = "총 주문 건수", example = "1")
        long orderCount,

        @Schema(description = "읽지 않은 알림 건수", example = "1")
        long unreadNotificationCount
) {
    public static UserMeResponse of(User user, long orderCount, long unreadNotificationCount) {
        String tryOnImageUrl = user.getTryOnImageUrl();
        boolean hasTryOnImage = tryOnImageUrl != null && !tryOnImageUrl.isBlank();

        return new UserMeResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getRole().name(),
                user.getPhone(),
                user.getHeight(),
                user.getWeight(),
                user.getAgeGroup() != null ? user.getAgeGroup().name() : null,
                user.getAddress(),
                user.getProfileImageUrl(),
                tryOnImageUrl,
                hasTryOnImage,
                orderCount,
                unreadNotificationCount
        );
    }
}
