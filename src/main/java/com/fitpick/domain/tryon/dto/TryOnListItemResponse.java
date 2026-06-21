package com.fitpick.domain.tryon.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "가상 착용 목록 항목")
public record TryOnListItemResponse(

        @Schema(description = "가상 착용 ID", example = "1")
        Long tryOnId,

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "상품명", example = "에어리즘 코튼 오버사이즈 티셔츠")
        String clothesTitle,

        @Schema(description = "상품 옵션 ID", example = "41")
        Long clothesOptionId,

        @Schema(description = "처리 상태", example = "DONE")
        String status,

        @Schema(description = "원본 전신 이미지 URL")
        String originalImageUrl,

        @Schema(description = "상품 이미지 URL")
        String productImageUrl,

        @Schema(description = "생성된 가상 착용 이미지 URL")
        String generatedImageUrl,

        @Schema(description = "요청 생성 시각", example = "2026-06-22T15:30:00")
        LocalDateTime createdAt
) {
}
