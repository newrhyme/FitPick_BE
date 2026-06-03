package com.fitpick.domain.nfc.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum NfcErrorCode implements ErrorCode {

    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "등록되지 않았거나 비활성화된 NFC 태그입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    NfcErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
