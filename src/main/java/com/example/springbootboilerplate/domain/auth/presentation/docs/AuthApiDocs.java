package com.example.springbootboilerplate.domain.auth.presentation.docs;

import com.example.springbootboilerplate.domain.auth.dto.AuthTokenResponse;
import com.example.springbootboilerplate.domain.auth.dto.LoginRequest;
import com.example.springbootboilerplate.domain.auth.dto.SignupRequest;
import com.example.springbootboilerplate.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "회원가입/로그인 API")
public interface AuthApiDocs {

    @Operation(
            summary = "회원가입",
            description = "이메일, 비밀번호, 닉네임으로 회원가입 합니다."
    )
    ApiResponse<?> signup(@Valid @RequestBody SignupRequest signupRequest);

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인 합니다."
    )
    ApiResponse<?> login(@Valid @RequestBody LoginRequest loginRequest);
}
