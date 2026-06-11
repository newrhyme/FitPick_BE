package com.fitpick.domain.auth.controller.docs;

import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "회원가입/로그인 API")
public interface AuthApiDocs {

    @Operation(
            summary = "회원가입",
            description = "아이디, 비밀번호, 이름 등으로 회원가입 합니다. 역할은 서버에서 CUSTOMER로 고정됩니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (loginId/password/name 필수)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "아이디 중복 (A001)")
    })
    ApiResponse<?> signup(@Valid @RequestBody SignupRequest signupRequest);

    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호로 로그인합니다. 성공 시 JWT access token과 사용자 정보(role, ageGroup 등)를 반환합니다."
    )
    @SecurityRequirements
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공 — accessToken + user 정보 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치 (A002)")
    })
    ApiResponse<?> login(@Valid @RequestBody LoginRequest loginRequest);
}
