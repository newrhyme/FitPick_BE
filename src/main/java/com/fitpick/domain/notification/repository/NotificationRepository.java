package com.fitpick.domain.notification.repository;

import com.fitpick.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 목록 (최신순 정렬은 Pageable의 Sort로 주입)
    Page<Notification> findByUserId(Long userId, Pageable pageable);

    // 마이페이지 읽지 않은 알림 건수
    long countByUserIdAndIsReadFalse(Long userId);
}
