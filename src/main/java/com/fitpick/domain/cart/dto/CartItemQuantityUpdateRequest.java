package com.fitpick.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 수량 변경 요청")
public record CartItemQuantityUpdateRequest(

        @Schema(description = "변경할 수량 (1 이상)", example = "3", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
