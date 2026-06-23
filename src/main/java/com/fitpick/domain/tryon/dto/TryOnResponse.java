package com.fitpick.domain.tryon.dto;

import com.fitpick.domain.tryon.entity.TryOn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "가상 착용 단건 응답")
public record TryOnResponse(

        @Schema(description = "가상 착용 ID", example = "1")
        Long tryOnId,

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "상품 옵션 ID", example = "41")
        Long clothesOptionId,

        @Schema(description = "사이즈", example = "L")
        String size,

        @Schema(description = "색상", example = "네이비")
        String color,

        @Schema(description = "처리 상태 (PENDING/PROCESSING/DONE/FAILED)", example = "DONE")
        String status,

        @Schema(description = "원본 전신 이미지 URL", example = "https://fitpick-images.s3.ap-northeast-2.amazonaws.com/users/9/try-on/abc.png")
        String originalImageUrl,

        @Schema(description = "OpenAI에 보낸 상품 이미지 URL")
        String productImageUrl,

        @Schema(description = "생성된 가상 착용 이미지 URL (DONE일 때만 값 존재)")
        String generatedImageUrl,

        @Schema(description = "요청 생성 시각", example = "2026-06-22T15:30:00")
        LocalDateTime createdAt
) {
    public static TryOnResponse of(TryOn t, Long clothesId, Long clothesOptionId, String size, String color) {
        return new TryOnResponse(
                t.getId(),
                clothesId,
                clothesOptionId,
                size,
                color,
                t.getStatus().name(),
                t.getOriginalImageUrl(),
                t.getProductImageUrl(),
                t.getGeneratedImageUrl(),
                t.getCreatedAt()
        );
    }
}
