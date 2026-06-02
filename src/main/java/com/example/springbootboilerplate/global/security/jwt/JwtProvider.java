package com.example.springbootboilerplate.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
public class JwtProvider {

    private final JwtProperties props;
    private final Key key;

    public JwtProvider(JwtProperties props) {
        this.props =props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    // 액세스 토큰을 생성하는 메서드
    public String generateAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + props.accessTokenExpirationMs());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // 토큰이 유효한지 검증하는 메서드
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

    // 토큰에서 사용자 ID를 추출하는 메서드
    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    // 토큰에서 사용자 역할을 추출하는 메서드
    public String getRole(String token) {
        Object role = parse(token).getPayload().get("role");
        return role == null ? null : role.toString();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException| IllegalArgumentException e) {
            return false;
        }
    }
}
