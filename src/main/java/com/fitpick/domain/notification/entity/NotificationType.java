package com.fitpick.domain.notification.entity;

public enum NotificationType {

    PICKUP_READY("상품 준비 완료", "주문하신 상품이 준비되었습니다."),
    TRY_ON_DONE("가상 피팅 완료", "요청하신 가상 피팅 이미지가 준비되었습니다."),
    TRY_ON_FAILED("가상 피팅 실패", "가상 피팅 이미지 생성에 실패했습니다. 다시 시도해주세요.");

    private final String title;
    private final String body;

    NotificationType(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() { return title; }
    public String getBody() { return body; }
}
