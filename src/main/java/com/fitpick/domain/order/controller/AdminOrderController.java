package com.fitpick.domain.order.controller;

import com.fitpick.domain.order.controller.docs.AdminOrderApiDocs;
import com.fitpick.domain.order.dto.AdminOrderDetailResponse;
import com.fitpick.domain.order.dto.AdminOrderStatsResponse;
import com.fitpick.domain.order.dto.AdminOrderSummaryResponse;
import com.fitpick.domain.order.dto.OrderStatusUpdateRequest;
import com.fitpick.domain.order.entity.OrderStatus;
import com.fitpick.domain.order.service.AdminOrderService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import com.fitpick.global.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController implements AdminOrderApiDocs {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ApiResponse<?> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AdminOrderSummaryResponse> response = adminOrderService.getOrders(status, pageable);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<?> getOrder(
            @PathVariable Long orderId
    ) {
        AdminOrderDetailResponse response = adminOrderService.getOrder(orderId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }

    @PatchMapping("/{orderId}/status")
    public ApiResponse<?> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.updateStatus(orderId, request);
        return ApiResponse.success(SuccessCode.UPDATE_SUCCESS, response);
    }

    @GetMapping("/summary")
    public ApiResponse<?> getOrderStats() {
        AdminOrderStatsResponse response = adminOrderService.getOrderStats();
        return ApiResponse.success(SuccessCode.READ_SUCCESS, response);
    }
}
