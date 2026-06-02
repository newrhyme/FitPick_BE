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

    @Test
    void 회원가입_이미_존재하는_이메일이면_예외() {
        // given
        SignupRequest request = new SignupRequest("test1@example.com", "Password123!", "tester1");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(AuthErrorCode.EMAIL_ALREADY_EXISTS);
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    void 회원가입_성공하면_유저를_저장한다() {
        // given
        SignupRequest request = new SignupRequest("new@example.com", "Password123!", "tester1");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("ENCODED");

        // when
        authService.signUp(request);

        // then (save에 들어간 User를 캡쳐해서 값 검증)
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getNickname()).isEqualTo("tester1");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void 로그인_이메일이_없으면_예외() {
        // given
        LoginRequest request = new LoginRequest("nope@example.com", "Password123!");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

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
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword!");
        User user = User.create("test@example.com", "ENCODED", "tester1");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
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
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        User user = User.create("test@example.com", "ENCODED", "tester1");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtProvider.generateAccessToken(any(), anyString())).thenReturn("ACCESS_TOKEN");

        // when
        AuthTokenResponse res = authService.login(request);

        // then
        assertThat(res.accessToken()).isEqualTo("ACCESS_TOKEN");
        verify(jwtProvider).generateAccessToken(any(), eq(user.getEmail()));
    }
}
