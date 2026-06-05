package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(

        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus status
) {
}
