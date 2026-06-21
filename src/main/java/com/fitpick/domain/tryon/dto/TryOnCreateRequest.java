package com.fitpick.domain.tryon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가상 착용 요청")
public record TryOnCreateRequest(

        @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long clothesId,

        @Schema(description = "상품 옵션 ID", example = "41", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long clothesOptionId
) {
}
