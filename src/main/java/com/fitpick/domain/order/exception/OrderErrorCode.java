package com.fitpick.domain.order.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "OD001", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "OD002", "해당 주문에 대한 권한이 없습니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "OD003", "장바구니가 비어 있어 주문할 수 없습니다."),
    CLOTHES_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "OD004", "주문할 상품 옵션을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "OD005", "재고가 부족합니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "OD006", "현재 주문 상태에서는 수행할 수 없는 작업입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    OrderErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }

    @Override
    public String getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
