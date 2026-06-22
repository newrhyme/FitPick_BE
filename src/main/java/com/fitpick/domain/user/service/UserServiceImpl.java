package com.fitpick.domain.user.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.notification.repository.NotificationRepository;
import com.fitpick.domain.order.repository.OrderRepository;
import com.fitpick.domain.user.dto.UserMeResponse;
import com.fitpick.domain.user.dto.UserUpdateRequest;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public UserMeResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        long orderCount = orderRepository.countByUserId(userId);
        long unreadNotificationCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return UserMeResponse.of(user, orderCount, unreadNotificationCount);
    }

    @Override
    @Transactional
    public UserMeResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        user.updateProfile(
                request.phone(),
                request.height(),
                request.weight(),
                request.ageGroup(),
                request.address()
        );

        long orderCount = orderRepository.countByUserId(userId);
        long unreadNotificationCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return UserMeResponse.of(user, orderCount, unreadNotificationCount);
    }

    @Override
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
        user.updateFcmToken(fcmToken);
    }
}
