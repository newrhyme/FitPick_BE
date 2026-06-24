package com.fitpick.domain.notification.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.notification.dto.FcmTestRequest;
import com.fitpick.domain.notification.dto.NotificationResponse;
import com.fitpick.domain.notification.dto.TestFcmResponse;
import com.fitpick.domain.notification.entity.Notification;
import com.fitpick.domain.notification.entity.NotificationType;
import com.fitpick.domain.notification.exception.NotificationErrorCode;
import com.fitpick.domain.notification.repository.NotificationRepository;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.tryon.entity.TryOn;
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

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Override
    @Transactional
    public Notification notifyOrderStatusChange(Order order, String comment) {
        // 1) Notification DB 저장 — title은 "#{orderId}", body는 관리자가 입력한 코멘트.
        // 시연용으로 type은 PICKUP_READY 차용 (추후 상태별 분기 필요시 enum 확장).
        Notification saved = notificationRepository.save(Notification.create(
                order.getUserId(),
                order.getId(),
                "#" + order.getId(),
                comment,
                NotificationType.PICKUP_READY
        ));

        // 2) FCM 발송 — 토큰 없거나 FCM 비활성이어도 FcmService가 알아서 skip (예외 던지지 않음).
        User user = userRepository.findById(order.getUserId()).orElse(null);
        String token = (user != null) ? user.getFcmToken() : null;

        Map<String, String> data = new HashMap<>();
        data.put("orderId", String.valueOf(order.getId()));
        data.put("notificationId", String.valueOf(saved.getId()));

        fcmService.send(token, "#" + order.getId(), comment, data);

        return saved;
    }

    @Override
    @Transactional
    public Notification notifyTryOnDone(TryOn tryOn) {
        // 1) Notification DB 저장 — imageUrl 컬럼에 generatedImageUrl 그대로 저장 (조회 시 추가 lookup 없음).
        String title = NotificationType.TRY_ON_DONE.getTitle();
        String body = NotificationType.TRY_ON_DONE.getBody();
        Notification saved = notificationRepository.save(Notification.createForTryOn(
                tryOn.getUserId(),
                tryOn.getId(),
                title,
                body,
                tryOn.getGeneratedImageUrl(),
                NotificationType.TRY_ON_DONE
        ));

        // 2) FCM 발송 — 토큰 없거나 비활성이어도 skip (예외 던지지 않음).
        User user = userRepository.findById(tryOn.getUserId()).orElse(null);
        String token = (user != null) ? user.getFcmToken() : null;

        Map<String, String> data = new HashMap<>();
        data.put("tryOnId", String.valueOf(tryOn.getId()));
        data.put("notificationId", String.valueOf(saved.getId()));
        if (tryOn.getGeneratedImageUrl() != null) {
            data.put("generatedImageUrl", tryOn.getGeneratedImageUrl());
        }

        fcmService.send(token, title, body, data);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        // 읽지 않은(isRead=false) 알림만 반환
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId, pageable);
        Page<NotificationResponse> mapped = notifications.map(NotificationResponse::from);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
        }
        // idempotent — 이미 read=true여도 markAsRead는 그대로 true 유지
        notification.markAsRead();
        return NotificationResponse.from(notification);
    }

    @Override
    @Transactional
    public TestFcmResponse sendTestFcm(Long userId, FcmTestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        // 알림 DB 저장 — 클라이언트가 read 처리 호출 가능하게 함.
        // 시연 전 제거 예정 API라 type은 기존 PICKUP_READY 차용.
        Notification saved = notificationRepository.save(Notification.create(
                userId, null, request.title(), request.body(), NotificationType.PICKUP_READY
        ));

        // FCM data 페이로드: 클라이언트 data + notificationId 자동 주입.
        // Firebase 명세상 모든 값은 string이어야 함 (number/boolean은 String.valueOf로 변환).
        Map<String, String> fcmData = new HashMap<>();
        if (request.data() != null) {
            request.data().forEach((k, v) -> {
                if (v != null) fcmData.put(k, String.valueOf(v));
            });
        }
        fcmData.put("notificationId", String.valueOf(saved.getId()));

        FcmSendResult result = fcmService.send(
                user.getFcmToken(), request.title(), request.body(), fcmData);

        // 응답 data는 FCM에 보낸 것과 동일 (디바이스 수신과 같은 구조).
        return new TestFcmResponse(result.sent(), result.messageId(), result.reason(), fcmData);
    }
}
