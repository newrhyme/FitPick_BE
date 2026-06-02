package com.example.springbootboilerplate.global.security.jwt;

import com.example.springbootboilerplate.domain.user.infrastructure.UserRepository;
import com.example.springbootboilerplate.global.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveBearerToken(request);

        // 토큰이 유효하면 인증 정보를 SecurityContext에 저장
        if (token != null && jwtProvider.isValid(token)) {
            Long userId = jwtProvider.getUserId(token);

            // 사용자 ID로 사용자 정보를 조회하고 인증 객체 생성
            userRepository.findById(userId).ifPresent(user -> {
                CustomUserDetails principal = new CustomUserDetails(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }

    // 헤더에서 Bearer 토큰을 추출하는 메서드
    private String resolveBearerToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || auth.isBlank()) return null;
        if (!auth.startsWith("Bearer ")) return null;
        return auth.substring(7);
    }
}
