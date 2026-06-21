package com.fitpick.domain.order.entity;

import com.fitpick.domain.order.exception.OrderErrorCode;
import com.fitpick.global.common.entity.BaseTimeEntity;
import com.fitpick.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Order extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, Long storeId, OrderType orderType) {
        return Order.builder()
                .userId(userId)
                .storeId(storeId)
                .orderType(orderType)
                .status(OrderStatus.CREATED)
                .totalPrice(0)
                .build();
    }

    // 아이템 추가 + 양방향 연관 세팅 + 총액 누적
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.assignOrder(this);
        this.totalPrice += item.getPrice() * item.getQuantity();
    }

    // ===== 상태 전이 =====

    // CREATED -> PAID
    public void markPaid(LocalDateTime paidAt) {
        if (this.status != OrderStatus.CREATED) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.PAID;
        this.paidAt = paidAt;
    }

    // PAID -> PREPARING
    public void markPreparing() {
        if (this.status != OrderStatus.PAID) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.PREPARING;
    }

    // PREPARING -> READY
    public void markReady() {
        if (this.status != OrderStatus.PREPARING) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.READY;
    }

    // READY -> PICKED_UP
    public void markPickedUp() {
        if (this.status != OrderStatus.READY) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.PICKED_UP;
    }

    // CREATED/PAID/PREPARING -> CANCELED (READY 이후 불가)
    public void cancel() {
        if (this.status != OrderStatus.CREATED
                && this.status != OrderStatus.PAID
                && this.status != OrderStatus.PREPARING) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.CANCELED;
        // 재고 복구는 서비스에서: cancel() 호출 전 status가 PAID/PREPARING이었는지 보고 결정
    }

    // 관리자 취소: PAID/PREPARING/READY -> CANCELED (PICKED_UP/CANCELED 이후 불가)
    public void cancelByAdmin() {
        if (this.status != OrderStatus.PAID
                && this.status != OrderStatus.PREPARING
                && this.status != OrderStatus.READY) {
            throw new CustomException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        this.status = OrderStatus.CANCELED;
        // 재고 복구는 서비스에서: 호출 전 status가 PAID/PREPARING/READY이었는지 보고 결정
    }

}
