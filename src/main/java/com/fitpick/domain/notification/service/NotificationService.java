package com.fitpick.domain.notification.service;

import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.infra.firebase.FcmService.FcmSendResult;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // 주문 READY -> 픽업 준비완료 알림 저장 (1차는 DB 저장만하고 FCM은 나중에)
    void notifyPickupReady(Order order);

    // (추가) 내 알림 목록 조회
    PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    // [TEMP] 본인의 fcmToken으로 테스트 푸시. 3단계 통합 검증용 — 시연 전 제거 예정.
    FcmSendResult sendTestFcm(Long userId, FcmTestRequest request);
}
