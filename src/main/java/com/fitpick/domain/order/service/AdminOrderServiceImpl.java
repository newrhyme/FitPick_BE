package com.fitpick.domain.order.service;

import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.notification.service.NotificationService;
import com.fitpick.domain.order.dto.AdminOrderDetailResponse;
import com.fitpick.domain.order.dto.AdminOrderSummaryResponse;
import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.Order;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.domain.order.exception.OrderErrorCode;
import com.fitpick.domain.order.repository.OrderRepository;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
