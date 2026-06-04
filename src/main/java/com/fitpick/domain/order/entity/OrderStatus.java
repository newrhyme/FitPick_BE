package com.fitpick.domain.order.entity;

public enum OrderStatus {

    CREATED("주문 생성"),
    PAID("결제 완료"),
    PREPARING("상품 준비 중"),
    READY("픽업 대기"),
    PICKED_UP("픽업 완료"),
    CANCELED("주문 취소");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
