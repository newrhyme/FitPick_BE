package com.fitpick.domain.notification.controller.docs;

import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Notification", description = "알림 조회/읽음 처리 API")
public interface NotificationDocs {

    @Operation(summary = "내 알림 목록 조회", description = "로그인 사용자의 알림 목록 (최신순, 페이징)")
    ApiResponse<?> getMyNotifications(CustomUserDetails userDetails, @ParameterObject Pageable pageable);
}
