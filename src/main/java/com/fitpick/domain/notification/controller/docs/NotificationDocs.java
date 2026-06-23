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
            summary = "내 알림 목록 조회 (읽지 않은 알림만)",
            description = "로그인 사용자의 알림 중 isRead=false인 항목만 최신순으로 페이지네이션 조회합니다. " +
                          "안 읽은 알림이 없으면 빈 페이지를 반환합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — PageResponse<NotificationResponse> 반환")
    })
    ApiResponse<?> getMyNotifications(CustomUserDetails userDetails, @ParameterObject Pageable pageable);

    @Operation(
            summary = "알림 읽음 처리",
            description = "본인 알림만 읽음 처리 가능. 이미 읽음 상태여도 200 (idempotent). " +
                          "응답으로 갱신된 NotificationResponse를 반환합니다."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공 — NotificationResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 알림 아님 (NT002)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "알림 없음 (NT001)")
    })
    ApiResponse<?> markAsRead(CustomUserDetails userDetails, Long notificationId);

    @Operation(
            summary = "전체 알림 읽음 처리",
            description = "본인의 안 읽은(isRead=false) 알림 전부를 일괄 읽음 처리합니다. " +
                          "응답 data.updatedCount에 이번 호출로 처리된 건수가 반환됩니다 " +
                          "(이미 모두 읽은 상태였다면 0). idempotent — 여러 번 호출해도 안전."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "처리 성공 — MarkAllReadResponse(updatedCount) 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요 (E401)")
    })
    ApiResponse<?> markAllAsRead(CustomUserDetails userDetails);

    @Operation(
            summary = "[TEMP] FCM 테스트 발송 (notification + optional data payload)",
            description = "본인의 users.fcm_token으로 즉시 테스트 푸시를 발송하고, 동시에 notifications 테이블에 알림을 저장합니다 " +
                          "(이후 클라이언트가 read 처리할 수 있도록). " +
                          "메시지는 Firebase Cloud Messaging의 \"notification + data\" 형식으로 구성됩니다 " +
                          "(notification: title/body는 시스템 트레이 표시용, data: 앱이 받아 처리하는 키-값 페이로드). " +
                          "data 필드는 선택이며 모든 값은 string으로 전송됩니다(number/boolean 입력 시 자동 변환). " +
                          "서버가 data에 'notificationId' 키를 자동 주입하며, FCM 메시지 data와 응답 body data는 동일 구조 — " +
                          "클라이언트(디바이스)가 푸시 수신 즉시 data.notificationId로 read 처리 호출 가능. " +
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
