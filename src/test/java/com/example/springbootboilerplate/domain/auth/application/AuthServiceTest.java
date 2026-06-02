package com.example.springbootboilerplate.domain.auth.application;

import com.example.springbootboilerplate.domain.auth.dto.AuthTokenResponse;
import com.example.springbootboilerplate.domain.auth.dto.LoginRequest;
import com.example.springbootboilerplate.domain.auth.dto.SignupRequest;
import com.example.springbootboilerplate.domain.auth.exception.AuthErrorCode;
import com.example.springbootboilerplate.domain.user.domain.Role;
import com.example.springbootboilerplate.domain.user.domain.User;
import com.example.springbootboilerplate.domain.user.infrastructure.UserRepository;
import com.example.springbootboilerplate.global.exception.CustomException;
import com.example.springbootboilerplate.global.security.jwt.JwtProvider;
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

        // 보통 id는 DB에서 생기지만, 단위테스트에서는 필요하면 리플렉션/세터/테스트용 팩토리로 넣어도 됨.
        // 네 코드가 user.getId()를 쓰니까, 여기서 null이면 generateAccessToken 호출이 꼬일 수 있음.
        // 해결 1) JwtProvider generateAccessToken 파라미터를 email/subject로 바꾸기
        // 해결 2) User에 테스트용 생성자/팩토리 추가
        //
        // 일단 현재 AuthService는 generateAccessToken(user.getId(), user.getEmail())로 되어있으니
        // 아래처럼 "id가 null이어도" 호출 자체는 되게끔 anyLong() 대신 any()로 검증하고
        // jwtProvider 스텁도 any()로 둠.

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