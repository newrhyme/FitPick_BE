package com.fitpick.domain.notification.service;

import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.notification.dto.TestFcmResponse;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // 주문 READY -> 픽업 준비완료 알림 저장 (1차는 DB 저장만하고 FCM은 나중에)
    void notifyPickupReady(Order order);

    // (추가) 내 알림 목록 조회 — 읽지 않은(isRead=false) 알림만 반환
    PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    // 본인 알림 읽음 처리 (idempotent — 이미 읽음 상태여도 200)
    NotificationResponse markAsRead(Long userId, Long notificationId);

    // 본인의 안 읽은 알림 일괄 읽음 처리 — 반환: 영향받은 건수
    int markAllAsRead(Long userId);

    // [TEMP] 본인의 fcmToken으로 테스트 푸시. 3단계 통합 검증용 — 시연 전 제거 예정.
    // notification DB 저장 + data payload에 notificationId 자동 주입.
    TestFcmResponse sendTestFcm(Long userId, FcmTestRequest request);
}
