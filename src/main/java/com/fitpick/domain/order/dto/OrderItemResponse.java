package com.fitpick.domain.order.dto;

import com.fitpick.domain.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 항목")
public record OrderItemResponse(

        @Schema(description = "주문 항목 ID", example = "20")
        Long orderItemId,

        @Schema(description = "상품 ID — 상품 상세 화면 이동에 사용", example = "1")
        Long clothesId,

        @Schema(description = "상품 옵션 ID", example = "2")
        Long clothesOptionId,

        @Schema(description = "상품명", example = "오버핏 코튼 셔츠")
        String clothesTitle,

        @Schema(description = "사이즈", example = "M")
        String size,

        @Schema(description = "색상", example = "화이트")
        String color,

        @Schema(description = "주문 수량", example = "2")
        Integer quantity,

        @Schema(description = "주문 시점 단가 (원) — 스냅샷", example = "39000")
        Integer price
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getClothesOption().getClothes().getId(),
                item.getClothesOption().getId(),
                item.getClothesOption().getClothes().getTitle(),
                item.getClothesOption().getSize(),
                item.getClothesOption().getColor(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
