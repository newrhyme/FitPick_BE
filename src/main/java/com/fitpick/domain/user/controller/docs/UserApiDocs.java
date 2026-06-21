package com.fitpick.domain.user.controller.docs;

import com.fitpick.domain.user.dto.UserUpdateRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 정보 API")
public interface UserApiDocs {

    @Operation(
            summary = "마이페이지 정보 조회",
            description = "로그인한 사용자의 마이페이지 정보를 조회합니다. " +
                          "주문 건수, 읽지 않은 알림 건수, 가상 착용 이미지 여부를 포함합니다. " +
                          "CUSTOMER/STAFF 모두 호출 가능합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ApiResponse<?> getMyInfo(CustomUserDetails userDetails);

    @Operation(
            summary = "내 정보 수정",
            description = "phone/height/weight/ageGroup/address 부분 수정. " +
                          "null 또는 누락된 필드는 변경하지 않습니다. " +
                          "loginId/role/password/profileImageUrl/tryOnImageUrl은 이 API로 수정할 수 없습니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공 — 변경된 UserMeResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 본문 파싱 실패 또는 잘못된 ageGroup 값 (E000)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ApiResponse<?> updateMyInfo(CustomUserDetails userDetails, UserUpdateRequest request);
}
