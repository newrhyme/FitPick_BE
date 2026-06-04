package com.fitpick.domain.cart.entity;

import com.fitpick.domain.clothes.entity.ClothesOption;
import com.fitpick.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CartItem extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_option_id", nullable = false)
    private ClothesOption clothesOption;

    @Column(nullable = false)
    private Integer quantity;

    public static CartItem create(Cart cart, ClothesOption clothesOption, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .clothesOption(clothesOption)
                .quantity(quantity)
                .build();
    }

    // 수량 더하기
    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    // 수량 변경
    public void changeQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }
}
