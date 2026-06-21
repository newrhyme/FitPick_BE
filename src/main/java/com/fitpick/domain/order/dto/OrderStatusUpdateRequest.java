package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 상태 변경 요청 (관리자 전용)")
public record OrderStatusUpdateRequest(

        @Schema(
                description = "변경할 주문 상태. 허용 전이: PAID→PREPARING→READY→PICKED_UP, "
                        + "또는 PAID/PREPARING/READY → CANCELED (관리자 취소, 재고 복구 동반)",
                example = "PREPARING",
                allowableValues = {"PREPARING", "READY", "PICKED_UP", "CANCELED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "변경할 상태는 필수입니다.")
        OrderStatus status
) {
}
