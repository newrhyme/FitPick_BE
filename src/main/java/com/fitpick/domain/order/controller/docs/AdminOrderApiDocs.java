package com.fitpick.domain.order.controller.docs;

import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API (STAFF/ADMIN 전용 — JWT 필수)")
public interface AdminOrderApiDocs {

    @Operation(
            summary = "관리자 주문 목록 조회",
            description = "전체 주문 목록을 최신순으로 조회합니다. status 파라미터로 상태 필터링이 가능합니다. (STAFF/ADMIN 전용)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — PageResponse<AdminOrderSummaryResponse> 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 — STAFF/ADMIN만 접근 가능")
    })
    ApiResponse<?> getOrders(OrderStatus status, @ParameterObject Pageable pageable);

    @Operation(
            summary = "관리자 주문 상세 조회",
            description = "주문자 정보 + 주문 항목 + 결제/생성 시간을 포함한 상세 정보를 조회합니다. (STAFF/ADMIN 전용)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — AdminOrderDetailResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 — STAFF/ADMIN만 접근 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음 (OD001) 또는 주문자 계정 없음 (A005)")
    })
    ApiResponse<?> getOrder(Long orderId);

    @Operation(
            summary = "관리자 주문 상태 변경",
            description = "허용 전이: PAID→PREPARING→READY→PICKED_UP. READY로 변경 시 고객에게 픽업 준비완료 알림이 저장됩니다. (STAFF/ADMIN 전용)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공 — AdminOrderDetailResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (status 필수)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 — STAFF/ADMIN만 접근 가능"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음 (OD001) 또는 주문자 계정 없음 (A005)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "허용되지 않는 상태 전이 (OD006)")
    })
    ApiResponse<?> updateStatus(Long orderId, OrderStatusUpdateRequest request);

    @Operation(
            summary = "관리자 주문 요약 조회",
            description = "오늘 (Asia/Seoul 기준) 주문의 상태별 개수 조회,  (STAFF/ADMIN 전용)"
    )
    ApiResponse<?> getOrderStats();
}
