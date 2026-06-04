package com.fitpick.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequest(
        @NotNull Long optionId,
        @NotNull @Min(1) Integer quantity
) {
}
