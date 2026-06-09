package com.fitpick.domain.clothes.dto;

import com.fitpick.domain.clothes.entity.Clothes;
import com.fitpick.domain.clothes.entity.ClothesCategory;
import com.fitpick.domain.clothes.entity.ClothesImage;
import com.fitpick.domain.clothes.entity.ClothesOption;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "상품 상세 응답")
public record ClothesDetailResponse(

        @Schema(description = "상품 ID", example = "1")
        Long clothesId,

        @Schema(description = "상품명", example = "오버핏 코튼 셔츠")
        String title,

        @Schema(description = "상품 설명", example = "편안한 착용감의 오버핏 코튼 셔츠입니다.")
        String description,

        @Schema(description = "카테고리", example = "TOP")
        ClothesCategory category,

        @Schema(description = "소재", example = "면 100%")
        String material,

        @Schema(description = "가격 (원)", example = "39000")
        Integer price,

        @Schema(description = "썸네일 이미지 URL", example = "https://example.com/img/shirt_thumb.jpg")
        String thumbnailImageUrl,

        @Schema(description = "옵션 목록 (사이즈/색상/재고)")
        List<ClothesOptionResponse> options,

        @Schema(description = "이미지 목록 (정렬 순서 오름차순)")
        List<ClothesImageResponse> images
) {
    public static ClothesDetailResponse of(
            Clothes clothes,
            List<ClothesOption> options,
            List<ClothesImage> images
    ) {
        return new ClothesDetailResponse(
                clothes.getId(),
                clothes.getTitle(),
                clothes.getDescription(),
                clothes.getCategory(),
                clothes.getMaterial(),
                clothes.getPrice(),
                clothes.getThumbnailImageUrl(),
                options.stream().map(ClothesOptionResponse::from).toList(),
                images.stream().map(ClothesImageResponse::from).toList()
        );
    }
}
