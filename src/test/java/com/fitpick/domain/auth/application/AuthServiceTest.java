package com.fitpick.domain.auth.application;

import com.fitpick.domain.auth.dto.AuthTokenResponse;
import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.user.domain.Role;
import com.fitpick.domain.user.domain.User;
import com.fitpick.domain.user.infrastructure.UserRepository;
import com.fitpick.global.exception.CustomException;
import com.fitpick.global.security.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;

    @InjectMocks AuthService authService;

    private SignupRequest sampleSignup(String loginId) {
        return new SignupRequest(loginId, "Password123!", "홍길동", "010-0000-0000", 175, 70, "20대", "서울시");
    }

    @Test
    void 회원가입_이미_존재하는_아이디이면_예외() {
        // given
        SignupRequest request = sampleSignup("tester1");
        when(userRepository.existsByLoginId(request.loginId())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(AuthErrorCode.LOGIN_ID_ALREADY_EXISTS);
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    void 회원가입_성공하면_CUSTOMER_역할로_저장한다() {
        // given
        SignupRequest request = sampleSignup("newuser");
        when(userRepository.existsByLoginId(request.loginId())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("ENCODED");

        // when
        authService.signUp(request);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getLoginId()).isEqualTo("newuser");
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getName()).isEqualTo("홍길동");
        assertThat(saved.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void 로그인_아이디가_없으면_예외() {
        // given
        LoginRequest request = new LoginRequest("nope", "Password123!");
        when(userRepository.findByLoginId(request.loginId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
                });
    }

    @Test
    void 로그인_비밀번호가_틀리면_예외() {
        // given
        LoginRequest request = new LoginRequest("tester1", "WrongPassword!");
        User user = User.create("tester1", "ENCODED", "홍길동", null, null, null, null, null);

        when(userRepository.findByLoginId(request.loginId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
                });

        verify(jwtProvider, never()).generateAccessToken(any(), any());
    }

    @Test
    void 로그인_성공하면_토큰을_반환한다() {
        // given
        LoginRequest request = new LoginRequest("tester1", "Password123!");
        User user = User.create("tester1", "ENCODED", "홍길동", null, null, null, null, null);

        when(userRepository.findByLoginId(request.loginId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any(), anyString())).thenReturn("ACCESS_TOKEN");

        // when
        AuthTokenResponse res = authService.login(request);

        // then
        assertThat(res.accessToken()).isEqualTo("ACCESS_TOKEN");
        verify(jwtProvider).generateAccessToken(any(), eq(Role.CUSTOMER.name()));
    }
}
