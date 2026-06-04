package com.fitpick.domain.order.entity;

public enum OrderType {

    CART("장바구니 주문"),
    DIRECT("바로 구매");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
