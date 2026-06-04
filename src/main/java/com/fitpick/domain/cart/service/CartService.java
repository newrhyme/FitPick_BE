package com.fitpick.domain.cart.service;

import com.fitpick.domain.cart.dto.CartItemAddRequest;
import com.fitpick.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.fitpick.domain.cart.dto.CartResponse;

public interface CartService {

    // 담기
    CartResponse addItem(Long userId, CartItemAddRequest request);

    // 조회
    CartResponse getMyCart(Long userId);

    // 수량 변경
    CartResponse changeQuantity(Long userId, Long cartItemId, CartItemQuantityUpdateRequest request);

    // 삭제
    CartResponse removeItem(Long userId, Long cartItemId);
}
