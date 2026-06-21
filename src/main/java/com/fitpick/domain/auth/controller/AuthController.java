package com.fitpick.domain.auth.controller;

import com.fitpick.domain.auth.controller.docs.AuthApiDocs;
import com.fitpick.domain.auth.dto.LoginIdCheckResponse;
import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.LoginResponse;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.domain.auth.service.AuthService;
import com.fitpick.global.common.code.SuccessCode;
import com.fitpick.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
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
        LoginResponse loginResponse = authService.login(request);
        return ApiResponse.success(SuccessCode.LOGIN_SUCCESS, loginResponse);
    }

    @GetMapping("/check-login-id")
    public ApiResponse<?> checkLoginId(
            @RequestParam @NotBlank @Size(max = 50) String loginId
    ) {
        boolean available = authService.checkLoginIdAvailable(loginId);
        return ApiResponse.success(SuccessCode.READ_SUCCESS,
                new LoginIdCheckResponse(loginId, available));
    }
}
