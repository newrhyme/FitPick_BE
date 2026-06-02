package com.fitpick.domain.auth.controller;

import com.fitpick.domain.auth.controller.docs.AuthApiDocs;
import com.fitpick.domain.auth.dto.AuthTokenResponse;
import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.domain.auth.service.AuthService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<?> signup(@Valid @RequestBody SignupRequest request) {
        authService.signUp(request);
        return ApiResponse.success(SuccessCode.SIGNUP_SUCCESS);
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        AuthTokenResponse authTokenResponse = authService.login(request);
        return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, authTokenResponse);
    }
}
