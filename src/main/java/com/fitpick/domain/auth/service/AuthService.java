package com.fitpick.domain.auth.service;

import com.fitpick.domain.auth.dto.LoginRequest;
import com.fitpick.domain.auth.dto.LoginResponse;
import com.fitpick.domain.auth.dto.LoginUserResponse;
import com.fitpick.domain.auth.dto.SignupRequest;
import com.fitpick.domain.auth.exception.AuthErrorCode;
import com.fitpick.domain.user.entity.User;
import com.fitpick.domain.user.repository.UserRepository;
import com.fitpick.global.exception.CustomException;
import com.fitpick.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signUp(SignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(AuthErrorCode.LOGIN_ID_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
                request.loginId(),
                encodedPassword,
                request.name(),
                request.phone(),
                request.height(),
                request.weight(),
                request.ageGroup(),
                request.address()
        );
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean checkLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());

        return new LoginResponse(token, LoginUserResponse.from(user));
    }
}
