package com.fitpick.domain.notification.entity;

public enum NotificationType {

    ORDER("주문 알림", "주문 상태가 변경되었습니다."),
    TRYON("가상 피팅 완료", "요청하신 가상 피팅 이미지가 준비되었습니다.");

    private final String title;
    private final String body;

    NotificationType(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
}
