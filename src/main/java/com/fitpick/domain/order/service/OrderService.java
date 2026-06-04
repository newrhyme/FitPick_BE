package com.fitpick.domain.order.service;

import com.fitpick.domain.order.dto.DirectOrderRequest;
import com.fitpick.domain.order.dto.OrderResponse;
import com.fitpick.domain.order.dto.OrderSummaryResponse;
import com.fitpick.global.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    // CART : 장바구니 기반 주문 + mock 결제 (PAID 까지)
    OrderResponse orderFromCart(Long userId);

    // DIRECT : 바로 주문 + mock 결제 (PAID 까지)
    public OrderResponse orderDirect(Long userId, DirectOrderRequest request);

    // 주문 단건 조회
    OrderResponse getOrder(Long userId, Long orderId);

    // 내 주문 목록
    PageResponse<OrderSummaryResponse> getMyOrders(Long userId, Pageable pageable);

    // 취소 (CUSTOMER : 본인 주문, CRATED/PAID/PREPARING 에서만)
    OrderResponse cancelOrder(Long userId, Long orderId);
}
