package com.example.springbootboilerplate.global.common.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
