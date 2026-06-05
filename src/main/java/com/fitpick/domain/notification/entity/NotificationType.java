package com.fitpick.domain.notification.entity;

public enum NotificationType {

    PICKUP_READY("상품 준비 완료", "주문하신 상품이 준비되었습니다.");

    private final String title;
    private final String body;

    NotificationType(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
}
