package com.fitpick.domain.user.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ImageErrorCode implements ErrorCode {

    UNSUPPORTED_TYPE(HttpStatus.BAD_REQUEST, "I001", "지원하지 않는 이미지 형식입니다. (jpeg/png/webp만 허용)"),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "I002", "업로드된 파일이 비어 있습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "I003", "이미지 업로드에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ImageErrorCode(HttpStatus httpStatus, String code, String message) {
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
