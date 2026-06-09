package com.fitpick.domain.order.controller.docs;

import com.fitpick.domain.order.dto.DirectOrderRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Order", description = "주문 + mock 결제 API (로그인 필수)")
public interface OrderApiDocs {

    @Operation(summary = "장바구니 주문", description = "내 장바구니 항목 전체로 주문 생성 + mock 결제(PAID)까지 처리합니다. 주문 후 장바구니는 비워집니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 성공 — OrderResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "장바구니가 비어 있음 (OD003)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 옵션 없음 (OD004)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족 (OD005)")
    })
    ApiResponse<?> orderFromCart(CustomUserDetails userDetails);

    @Operation(summary = "바로 구매", description = "단일 옵션을 선택해 즉시 주문 생성 + mock 결제(PAID)까지 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "주문 성공 — OrderResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (clothesOptionId/quantity 필수)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 옵션 없음 (OD004)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족 (OD005)")
    })
    ApiResponse<?> orderDirect(CustomUserDetails userDetails, DirectOrderRequest request);

    @Operation(summary = "주문 단건 조회", description = "본인 주문 상세 조회 (주문 아이템 포함)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — OrderResponse 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문 아님 (OD002)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음 (OD001)")
    })
    ApiResponse<?> getOrder(CustomUserDetails userDetails, Long orderId);

    @Operation(summary = "내 주문 목록", description = "본인 주문 목록을 최신순으로 페이지네이션 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — PageResponse<OrderSummaryResponse> 반환")
    })
    ApiResponse<?> getMyOrders(CustomUserDetails userDetails, @ParameterObject Pageable pageable);

    @Operation(summary = "주문 취소", description = "CREATED/PAID/PREPARING 상태에서만 취소 가능합니다. PAID/PREPARING 취소 시 재고가 복구됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "취소 성공 — OrderResponse(CANCELED) 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 주문 아님 (OD002)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "주문 없음 (OD001)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "취소 불가능한 상태 (OD006) — READY/PICKED_UP/CANCELED")
    })
    ApiResponse<?> cancelOrder(CustomUserDetails userDetails, Long orderId);
}
