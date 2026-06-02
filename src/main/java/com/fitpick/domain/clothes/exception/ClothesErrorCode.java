package com.fitpick.domain.clothes.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ClothesErrorCode implements ErrorCode {
    CLOTHES_NOT_FOUND(HttpStatus.NOT_FOUND, "C001", "상품을 찾을 수 없습니다."),
    CLOTHES_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "상품 옵션을 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "C003", "재고가 부족합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ClothesErrorCode(HttpStatus httpStatus, String code, String message) {
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
