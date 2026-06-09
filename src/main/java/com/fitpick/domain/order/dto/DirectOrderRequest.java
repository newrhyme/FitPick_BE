package com.fitpick.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "바로 구매 요청")
public record DirectOrderRequest(

        @Schema(description = "주문할 상품 옵션 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "옵션 ID는 필수입니다.")
        Long clothesOptionId,

        @Schema(description = "주문 수량 (1 이상)", example = "1", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity
) {
}
