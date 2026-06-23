package com.fitpick.domain.notification.controller;

import com.fitpick.domain.notification.controller.docs.NotificationDocs;
import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.notification.dto.TestFcmResponse;
import com.fitpick.domain.notification.service.NotificationService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationDocs {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<?> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<NotificationResponse> response =
                notificationService.getMyNotifications(userDetails.getUserId(), pageable);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<?> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long notificationId
    ) {
        NotificationResponse response = notificationService.markAsRead(userDetails.getUserId(), notificationId);
        return ApiResponse.success(SuccessCode.OK, response);
    }

    // [TEMP] FCM 3단계 통합 검증용 — 시연 전 제거 예정.
    @PostMapping("/test-fcm")
    public ApiResponse<?> testFcm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FcmTestRequest request
    ) {
        TestFcmResponse result = notificationService.sendTestFcm(userDetails.getUserId(), request);
        return ApiResponse.success(SuccessCode.OK, result);
    }
}
