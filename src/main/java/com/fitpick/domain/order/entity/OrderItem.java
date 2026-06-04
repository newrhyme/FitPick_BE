package com.fitpick.domain.order.entity;

import com.fitpick.domain.clothes.entity.ClothesOption;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothes_option_id", nullable = false)
    private ClothesOption clothesOption;

    @Column(nullable = false)
    private Integer quantity;

    // 주문 당시 가격 스냅샷 (clothes.price를 복사)
    @Column(nullable = false)
    private Integer price;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OrderItem create(ClothesOption clothesOption, Integer quantity, Integer price) {
        return OrderItem.builder()
                .clothesOption(clothesOption)
                .quantity(quantity)
                .price(price)
                .build();
    }

    // Order.addItem()에서 양방향 연관 세팅용
    void assignOrder(Order order) {
        this.order = order;
    }
}
