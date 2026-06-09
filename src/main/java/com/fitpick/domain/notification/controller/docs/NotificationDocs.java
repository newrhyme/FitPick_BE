package com.fitpick.domain.notification.controller.docs;

import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Notification", description = "알림 조회 API (로그인 필수)")
public interface NotificationDocs {

    @Operation(
            summary = "내 알림 목록 조회",
            description = "로그인 사용자의 알림 목록을 최신순으로 페이지네이션 조회합니다. 알림이 없으면 빈 페이지를 반환합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — PageResponse<NotificationResponse> 반환")
    })
    ApiResponse<?> getMyNotifications(CustomUserDetails userDetails, @ParameterObject Pageable pageable);
}
