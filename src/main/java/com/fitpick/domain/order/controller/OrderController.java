package com.fitpick.domain.order.controller;

import com.fitpick.domain.order.controller.docs.OrderApiDocs;
import com.fitpick.domain.order.dto.DirectOrderRequest;
import com.fitpick.domain.order.dto.OrderResponse;
import com.fitpick.domain.order.dto.OrderSummaryResponse;
import com.fitpick.domain.order.service.OrderService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.common.response.PageResponse;
import com.fitpick.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController implements OrderApiDocs {

    private final OrderService orderService;

    @PostMapping("/cart")
    public ApiResponse<?> orderFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        OrderResponse response = orderService.orderFromCart(userDetails.getUserId());
        return ApiResponse.success(SuccessCode.OK, response);
    }

    @PostMapping("/direct")
    public ApiResponse<?> orderDirect(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DirectOrderRequest request
    ) {
       OrderResponse response = orderService.orderDirect(userDetails.getUserId(), request);
       return ApiResponse.success(SuccessCode.OK, response);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<?> getOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.getOrder(userDetails.getUserId(), orderId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @GetMapping
    public ApiResponse<?> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders(userDetails.getUserId(), pageable);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<?> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.cancelOrder(userDetails.getUserId(), orderId);
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }
}
