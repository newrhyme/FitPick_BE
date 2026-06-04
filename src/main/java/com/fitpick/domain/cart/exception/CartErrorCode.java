package com.fitpick.domain.cart.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CartErrorCode implements ErrorCode {

    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "장바구니에서 해당 항목을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "CT002", "재고가 부족합니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CT003", "수량은 1개 이상이어야 합니다."),
    FORBIDDEN_CART_ACCESS(HttpStatus.FORBIDDEN, "CT004", "본인의 장바구니만 접근할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CartErrorCode(HttpStatus httpStatus, String code, String message) {
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
