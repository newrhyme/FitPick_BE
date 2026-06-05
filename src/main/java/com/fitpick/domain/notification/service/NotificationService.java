package com.fitpick.domain.notification.service;

import com.fitpick.domain.order.entity.Order;

public interface NotificationService {

    // 주문 READY -> 픽업 준비완료 알림 저장 (1차는 DB 저장만하고 FCM은 나중에)
    void notifyPickupReady(Order order);
}
