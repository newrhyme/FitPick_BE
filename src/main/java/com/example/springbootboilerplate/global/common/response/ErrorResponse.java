package com.example.springbootboilerplate.global.common.response;

import com.example.springbootboilerplate.global.common.code.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;
    private final Object errors;
    private final OffsetDateTime timestamp;

    public ErrorResponse(String code, String message, Object errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
        this.timestamp = OffsetDateTime.now();
    }


    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, Object errors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getErrors() {
        return errors;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

}
