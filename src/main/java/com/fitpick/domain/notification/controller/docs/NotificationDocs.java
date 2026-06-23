package com.fitpick.domain.notification.controller.docs;

import com.fitpick.domain.notification.dto.FcmTestRequest;
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

    @Operation(
            summary = "[TEMP] FCM 테스트 발송 (notification + optional data payload)",
            description = "본인의 users.fcm_token으로 즉시 테스트 푸시를 발송합니다. " +
                          "메시지는 Firebase Cloud Messaging의 \"notification + data\" 형식으로 구성됩니다 " +
                          "(notification: title/body는 시스템 트레이 표시용, data: 앱이 받아 처리하는 키-값 페이로드). " +
                          "data 필드는 선택이며 모든 값은 string으로 전송됩니다(number/boolean 입력 시 자동 변환). " +
                          "응답의 sent=true면 발송 성공, false면 reason 필드에 사유(FCM_DISABLED / TOKEN_EMPTY / 실제 발송 오류 메시지). " +
                          "FCM 3단계(Firebase Admin SDK 통합) 검증용 임시 API — 시연 전 제거 예정."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 완료 — sent/messageId/reason 반환 (발송 실패도 200으로 응답하며 reason에 사유)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "title/body 누락·공백·길이 초과 (E000)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음 (A005)")
    })
    ApiResponse<?> testFcm(CustomUserDetails userDetails, FcmTestRequest request);
}
