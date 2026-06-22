package com.fitpick.domain.notification.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.notification.entity.Notification;
import com.fitpick.domain.notification.entity.NotificationType;
import com.fitpick.domain.notification.repository.NotificationRepository;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.exception.CustomException;
import com.fitpick.global.infra.firebase.FcmService;
import com.fitpick.global.infra.firebase.FcmService.FcmSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        Page<NotificationResponse> mapped = notifications.map(NotificationResponse::from);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public FcmSendResult sendTestFcm(Long userId, FcmTestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        return fcmService.send(user.getFcmToken(), request.title(), request.body(), null);
    }
}
