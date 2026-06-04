package com.fitpick.domain.cart.controller;

import com.fitpick.domain.cart.controller.docs.CartApiDocs;
import com.fitpick.domain.cart.dto.CartItemAddRequest;
import com.fitpick.domain.cart.dto.CartItemQuantityUpdateRequest;
import com.fitpick.domain.cart.dto.CartResponse;
import com.fitpick.domain.cart.service.CartService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController implements CartApiDocs {

    private final CartService cartService;

    @PostMapping("/items")
    public ApiResponse<?> addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CartItemAddRequest request
    ) {
        CartResponse response = cartService.addItem(userDetails.getUserId(), request);
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @GetMapping
    public ApiResponse<?> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CartResponse response = cartService.getMyCart(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ApiResponse<?> changeQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestBody CartItemQuantityUpdateRequest request
    ) {
        CartResponse response = cartService.changeQuantity(userDetails.getUserId(), cartItemId, request);
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<?> removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId
    ) {
        CartResponse response = cartService.removeItem(userDetails.getUserId(), cartItemId);
        return ApiResponse.success(SuccessCode.OK, response);
    }
}