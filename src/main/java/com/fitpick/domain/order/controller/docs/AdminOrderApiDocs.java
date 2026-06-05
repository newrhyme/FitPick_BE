package com.fitpick.domain.order.controller.docs;

import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API (STAFF/ADMIN)")
public interface AdminOrderApiDocs {

    @Operation(summary = "관리자 주문 목록 조회", description = "전체 주문 목록 (status 필터 가능, 최신순 페이징)")
    ApiResponse<?> getOrders(OrderStatus status, @ParameterObject Pageable pageable);

    @Operation(summary = "관리자 주문 상세 조회", description = "주문자 정보 + 주문 항목 + 결제/주문 시간 포함")
    ApiResponse<?> getOrder(Long orderId);

    @Operation(summary = "관리자 주문 상태 변경",
            description = "PAID→PREPARING→READY→PICKED_UP. READY 변경 시 픽업 준비완료 알림 저장")
    ApiResponse<?> updateStatus(Long orderId, OrderStatusUpdateRequest request);

}
