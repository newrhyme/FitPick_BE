package com.fitpick.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 담기 요청")
public record CartItemAddRequest(

        @Schema(description = "담을 상품 옵션 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long optionId,

        @Schema(description = "담을 수량 (1 이상)", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Min(1) Integer quantity
) {
}
