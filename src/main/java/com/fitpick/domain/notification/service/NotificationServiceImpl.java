package com.fitpick.domain.notification.service;

import com.fitpick.domain.notification.entity.Notification;
import com.fitpick.domain.notification.entity.NotificationType;
import com.fitpick.domain.notification.repository.NotificationRepository;
import com.fitpick.domain.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void notifyPickupReady(Order order) {
        NotificationType type = NotificationType.PICKUP_READY;

        Notification notification = Notification.create(
                order.getUserId(),      // 알림 받을 사람 = 주문자
                order.getId(),          // 주문 연관 (order_id 채움)
                type.getTitle(),
                type.getBody(),
                type
        );

        notificationRepository.save(notification);

        // FCM 실제 발송은 후순위 — 나중에 여기서 fcmSender.send(...) 호출
    }
}
