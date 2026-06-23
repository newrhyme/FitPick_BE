package com.fitpick.domain.notification.repository;

import com.fitpick.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 목록 — 안 읽은(isRead=false) 알림만 (최신순 정렬은 Pageable의 Sort로 주입)
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

    // 마이페이지 읽지 않은 알림 건수
    long countByUserIdAndIsReadFalse(Long userId);

    // 본인의 모든 안 읽은 알림을 일괄 읽음 처리 — 반환: 영향받은 행 수
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true "
            + "WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
