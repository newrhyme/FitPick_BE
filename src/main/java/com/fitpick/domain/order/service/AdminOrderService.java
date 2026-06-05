package com.fitpick.domain.order.service;

import com.fitpick.domain.order.dto.AdminOrderDetailResponse;
import com.fitpick.domain.order.dto.AdminOrderSummaryResponse;
import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AdminOrderService {

    // 전체 주문 목록 (status 필터 + 최신순 페이징)
    PageResponse<AdminOrderSummaryResponse> getOrders(OrderStatus status, Pageable pageable);

    // 주문 상세 (주문자 정보 포함)
    AdminOrderDetailResponse getOrder(Long orderId);

    // 상태 변경 (PAID -> PREPARING -> PICKED_UP)
    AdminOrderDetailResponse updateStatus(Long orderId, OrderStatusUpdateRequest request);
}