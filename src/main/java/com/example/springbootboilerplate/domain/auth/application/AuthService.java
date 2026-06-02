package com.example.springbootboilerplate.domain.auth.application;

import com.example.springbootboilerplate.domain.auth.dto.AuthTokenResponse;
import com.example.springbootboilerplate.domain.auth.dto.LoginRequest;
import com.example.springbootboilerplate.domain.auth.dto.SignupRequest;
import com.example.springbootboilerplate.domain.auth.exception.AuthErrorCode;
import com.example.springbootboilerplate.domain.user.domain.User;
import com.example.springbootboilerplate.domain.user.infrastructure.UserRepository;
import com.example.springbootboilerplate.global.exception.CustomException;
import com.example.springbootboilerplate.global.security.jwt.JwtProvider;
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
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(request.email(), encodedPassword, request.nickname());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateAccessToken(
                user.getId(), user.getEmail()
        );

        return new AuthTokenResponse(token);
    }
}
