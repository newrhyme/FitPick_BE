package com.fitpick.domain.auth.presentation.docs;

import com.fitpick.domain.auth.dto.AuthTokenResponse;
import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "회원가입/로그인 API")
public interface AuthApiDocs {

    @Operation(
            summary = "회원가입",
            description = "아이디, 비밀번호, 이름 등으로 회원가입 합니다. 역할은 서버에서 CUSTOMER로 고정됩니다."
    )
    ApiResponse<?> signup(@Valid @RequestBody SignupRequest signupRequest);

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인 합니다."
    )
    ApiResponse<?> login(@Valid @RequestBody LoginRequest loginRequest);
}
