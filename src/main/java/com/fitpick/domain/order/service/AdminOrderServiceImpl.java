package com.fitpick.domain.order.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.notification.service.NotificationService;
import com.fitpick.domain.order.dto.AdminOrderDetailResponse;
import com.fitpick.domain.order.dto.AdminOrderStatsResponse;
import com.fitpick.domain.order.dto.AdminOrderSummaryResponse;
import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.domain.order.exception.OrderErrorCode;
import com.fitpick.domain.order.repository.OrderRepository;
import com.fitpick.domain.order.repository.OrderStatusCountProjection;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> getOrders(OrderStatus status, Pageable pageable) {
        // status 분기: 있으면 필터, 없으면 전체
        Page<Order> orders = (status == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);

        Page<AdminOrderSummaryResponse> mapped = orders.map(AdminOrderSummaryResponse::from);
        return PageResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrder(Long orderId) {
        // items + clothesOption + clothes fetch join (Lazy 방지) — 고객용과 동일 쿼리 재사용
        Order order = orderRepository.findByIdWithItemsAndClothes(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        // 주문자 정보 별도 조회 (Order에 User 연관 없이 Long userId만 들고 있으므로)
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        return AdminOrderDetailResponse.of(order, user);
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse updateStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByIdWithItemsAndClothes(orderId)
                .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        // 요청 상태로 전이 — 엔티티 mark 메서드가 유효성 검증(잘못된 전이면 INVALID_STATUS_TRANSITION)
        switch (request.status()) {
            case PREPARING -> order.markPreparing();
            case READY     -> order.markReady();
            case PICKED_UP -> order.markPickedUp();
            default -> throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        // READY로 바뀌면 픽업 준비완료 알림 저장
        if (request.status() == OrderStatus.READY) {
            notificationService.notifyPickupReady(order);
        }

        // 주문자 정보 합쳐 상세 응답
        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

        return AdminOrderDetailResponse.of(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderStatsResponse getOrderStats() {
        // 1) 오늘 범위 (ASIA/Seoul 기준)
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // 2) status별 count 조회
        List<OrderStatusCountProjection> results = orderRepository.countTodayOrdersGroupByStatus(start, end);

        // 3) Map으로 변환 (없는 status는 0으로 처리)
        Map<OrderStatus, Long> countMap = results.stream()
                .collect(Collectors.toMap(
                        OrderStatusCountProjection::getStatus,
                        OrderStatusCountProjection::getCount
                ));

        long paidCount       = countMap.getOrDefault(OrderStatus.PAID, 0L);
        long preparingCount  = countMap.getOrDefault(OrderStatus.PREPARING, 0L);
        long readyCount      = countMap.getOrDefault(OrderStatus.READY, 0L);
        long pickedUpCount   = countMap.getOrDefault(OrderStatus.PICKED_UP, 0L);
        long canceledCount   = countMap.getOrDefault(OrderStatus.CANCELED, 0L);
        long todayTotalCount = paidCount + preparingCount + readyCount + pickedUpCount + canceledCount;

        return new AdminOrderStatsResponse(
                todayTotalCount,
                paidCount,
                preparingCount,
                readyCount,
                pickedUpCount,
                canceledCount
        );
    }
}
