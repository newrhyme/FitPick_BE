package com.fitpick.domain.cart.controller.docs;

import com.fitpick.domain.cart.dto.CartItemAddRequest;
import com.fitpick.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cart", description = "장바구니 API (로그인 필수)")
public interface CartApiDocs {

    @Operation(summary = "장바구니 담기", description = "옵션과 수량을 장바구니에 담습니다. 같은 옵션은 수량이 합산됩니다.")
    ApiResponse<?> addItem(CustomUserDetails userDetails, CartItemAddRequest request);

    @Operation(summary = "내 장바구니 조회", description = "내 장바구니의 항목, 총 수량, 총액을 조회합니다.")
    ApiResponse<?> getMyCart(CustomUserDetails userDetails);

    @Operation(summary = "장바구니 항목 수량 변경", description = "장바구니 항목의 수량을 변경합니다.")
    ApiResponse<?> changeQuantity(CustomUserDetails userDetails, Long cartItemId, CartItemQuantityUpdateRequest request);

    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 항목을 제거합니다.")
    ApiResponse<?> removeItem(CustomUserDetails userDetails, Long cartItemId);
}