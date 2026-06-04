package com.fitpick.domain.clothes.entity;

import com.fitpick.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clothes_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ClothesOption extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_id", nullable = false)
    private Clothes clothes;

    @Column(length = 20)
    private String size;

    @Column(length = 30)
    private String color;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    public static ClothesOption create(Clothes clothes, String size, String color, Integer stockQuantity) {
        return ClothesOption.builder()
                .clothes(clothes)
                .size(size)
                .color(color)
                .stockQuantity(stockQuantity != null ? stockQuantity : 0)
                .build();
    }

    @Builder
    private ClothesOption(Clothes clothes, String size, String color, Integer stockQuantity) {
        this.clothes = clothes;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
    }

    // 재고 차감 — mock 결제 완료(PAID 전이) 시점에 호출. 부족하면 예외.
    public void decreaseStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }
}
