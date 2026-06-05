package com.fitpick.domain.order.controller.docs;

import com.fitpick.domain.order.dto.DirectOrderRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Order", description = "주문 + mock 결제 API")
public interface OrderApiDocs {

    @Operation(summary = "장바구니 주문", description = "내 장바구니 항목으로 주문 + mock 결제(PAID)까지 처리")
    ApiResponse<?> orderFromCart(CustomUserDetails userDetails);

    @Operation(summary = "바로 구매", description = "단일 옵션 바로 구매 + mock 결제(PAID)까지 처리")
    ApiResponse<?> orderDirect(CustomUserDetails userDetails, DirectOrderRequest request);

    @Operation(summary = "주문 단건 조회", description = "본인 주문 상세 조회 (아이템 포함)")
    ApiResponse<?> getOrder(CustomUserDetails userDetails, Long orderId);

    @Operation(summary = "내 주문 목록", description = "본인 주문 목록(최신순, 페이지네이션)")
    ApiResponse<?> getMyOrders(CustomUserDetails userDetails, @ParameterObject Pageable pageable);

    @Operation(summary = "주문 취소", description = "CREATED/PAID/PREPARING 상태에서만 취소 가능. PAID/PREPARING은 재고 복구")
    ApiResponse<?> cancelOrder(CustomUserDetails userDetails, Long orderId);
}
