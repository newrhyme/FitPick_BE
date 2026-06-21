package com.fitpick.domain.tryon.exception;

import com.fitpick.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TryOnErrorCode implements ErrorCode {

    NO_TRY_ON_IMAGE(HttpStatus.BAD_REQUEST, "T001", "가상 착용용 전신 사진을 먼저 등록해주세요."),
    NO_PRODUCT_IMAGE(HttpStatus.BAD_REQUEST, "T002", "상품 이미지가 없어 가상 착용을 진행할 수 없습니다."),
    GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "T003", "가상 착용 이미지 생성에 실패했습니다."),
    TRY_ON_NOT_FOUND(HttpStatus.NOT_FOUND, "T004", "가상 착용 기록을 찾을 수 없습니다."),
    TRY_ON_ACCESS_DENIED(HttpStatus.FORBIDDEN, "T005", "본인의 가상 착용 기록만 조회할 수 있습니다."),
    OPTION_NOT_BELONG_TO_CLOTHES(HttpStatus.BAD_REQUEST, "T006", "선택한 옵션이 해당 상품에 속하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TryOnErrorCode(HttpStatus httpStatus, String code, String message) {
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
