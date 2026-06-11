package com.fitpick.domain.order.repository;

import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 내 주문 목록 (최신순 + 페이징)
    Page<Order> findByUserId(Long userId, Pageable pageable);

    // 주문 단건 조회
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    @Query("""
        select distinct o
        from Order o
        left join fetch o.items i
        left join fetch i.clothesOption co
        left join fetch co.clothes c
        where o.id = :orderId
    """)
    Optional<Order> findByIdWithItemsAndClothes(@Param("orderId") Long orderId);

    // status 필터용 (admin 목록)
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
        select o.status AS status, COUNT(o) AS count
        from Order o
        where o.createdAt >= :start
        and o.createdAt < :end
        group by o.status
    """)
    List<OrderStatusCountProjection> countTodayOrdersGroupByStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
