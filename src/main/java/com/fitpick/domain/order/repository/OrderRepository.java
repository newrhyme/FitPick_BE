package com.fitpick.domain.order.repository;

import com.fitpick.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 내 주문 목록 (최신순 + 페이징)
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // 주문 단건 조회
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
