package com.fitpick.domain.cart.controller.docs;

import com.fitpick.domain.cart.dto.CartItemAddRequest;
import com.fitpick.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Cart", description = "장바구니 API (로그인 필수)")
public interface CartApiDocs {

    @Operation(summary = "장바구니 담기", description = "옵션과 수량을 장바구니에 담습니다. 같은 옵션은 수량이 합산됩니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "담기 성공 — 최신 장바구니 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (optionId/quantity 필수)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품 옵션 없음 (C002)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족 (CT002)")
    })
    ApiResponse<?> addItem(CustomUserDetails userDetails, CartItemAddRequest request);

    @Operation(summary = "내 장바구니 조회", description = "내 장바구니의 항목, 총 수량, 총액을 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 — 장바구니 없으면 빈 CartResponse 반환")
    })
    ApiResponse<?> getMyCart(CustomUserDetails userDetails);

    @Operation(summary = "장바구니 항목 수량 변경", description = "장바구니 항목의 수량을 변경합니다. 수량은 1 이상이어야 합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수량 변경 성공 — 최신 장바구니 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "수량 1 미만 (CT003)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 장바구니 아님 (CT004)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장바구니 항목 없음 (CT001)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "재고 부족 (CT002)")
    })
    ApiResponse<?> changeQuantity(CustomUserDetails userDetails, Long cartItemId, CartItemQuantityUpdateRequest request);

    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 항목을 제거합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공 — 최신 장바구니 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 장바구니 아님 (CT004)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장바구니 항목 없음 (CT001)")
    })
    ApiResponse<?> removeItem(CustomUserDetails userDetails, Long cartItemId);

    @Operation(summary = "장바구니 전체 비우기", description = "내 장바구니의 모든 항목을 삭제합니다. 이미 비어있어도 204를 반환합니다(멱등).")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "비우기 성공 — 본문 없음")
    })
    ResponseEntity<ApiResponse<Void>> clearCart(CustomUserDetails userDetails);
}
