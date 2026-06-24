package com.fitpick.domain.tryon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "가상 착용 요청")
public record TryOnCreateRequest(

        @Schema(description = "상품 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long clothesId,

        @Schema(description = "상품 옵션 ID", example = "41", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long clothesOptionId,

        @Schema(
                description = "배경/분위기 요청 (선택). 자유 텍스트로 분위기·장소·상황 모두 가능. " +
                              "예: '밝은 분위기', '바닷가', '카페에서 친구 만나는 자리'. " +
                              "값이 있으면 자동 TPO 매핑보다 우선 적용됨. null/공백 가능.",
                example = "바닷가",
                maxLength = 200
        )
        @Size(max = 200, message = "style은 최대 200자입니다.")
        String style
) {
}
