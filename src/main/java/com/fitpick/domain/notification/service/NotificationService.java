package com.fitpick.domain.notification.service;

import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.notification.dto.TestFcmResponse;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.tryon.entity.TryOn;
import com.fitpick.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // 주문 상태 변경 시 알림 생성 + FCM 발송.
    // title = "#{orderId}", body = comment, data = { orderId, notificationId } (모두 string).
    // FCM 토큰 없는 사용자도 알림 DB 저장은 그대로 진행 (FCM만 skip).
    com.fitpick.domain.notification.entity.Notification notifyOrderStatusChange(Order order, String comment);

    // 가상 피팅 완료 시 FCM 발송만 수행 (알림 DB 저장 X — /api/v1/notifications 목록에 노출하지 않음).
    // data = { tryOnId, generatedImageUrl } (모두 string).
    // 호출 전 tryOn.status == DONE 이어야 하며 generatedImageUrl 이 채워져 있어야 함.
    void notifyTryOnDone(TryOn tryOn);

    // 가상 피팅 실패 시 FCM 발송만 수행 (알림 DB 저장 X — /api/v1/notifications 목록에 노출하지 않음).
    // data = { tryOnId } (string). 비동기 처리 흐름에서 실패 케이스를 프론트에 알리기 위함.
    void notifyTryOnFailed(Long userId, Long tryOnId);

    // (추가) 내 알림 목록 조회 — 읽지 않은(isRead=false) 알림만 반환
    PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    // 본인의 안 읽은(isRead=false) 알림 개수 — 레드닷 표시용
    long getUnreadCount(Long userId);

    // 본인 알림 읽음 처리 (idempotent — 이미 읽음 상태여도 200)
    NotificationResponse markAsRead(Long userId, Long notificationId);

    // 본인의 안 읽은 알림 일괄 읽음 처리 — 반환: 영향받은 건수
    int markAllAsRead(Long userId);

    // [TEMP] 본인의 fcmToken으로 테스트 푸시. 3단계 통합 검증용 — 시연 전 제거 예정.
    // notification DB 저장 + data payload에 notificationId 자동 주입.
    TestFcmResponse sendTestFcm(Long userId, FcmTestRequest request);
}
