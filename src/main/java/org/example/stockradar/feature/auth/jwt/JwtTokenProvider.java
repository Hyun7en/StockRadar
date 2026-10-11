package org.example.stockradar.feature.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 30;       // 30분
    private final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24; // 1일

    private final Key key;

    // 서명 키는 환경변수 JWT_SECRET 으로만 주입 (HS256 → 32바이트 이상)
    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 환경변수가 비어 있거나 32바이트 미만입니다.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String memberId) {
        return Jwts.builder()
                .setSubject(memberId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getMemberIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //  RefreshToken의 유효기간 반환 메서드 추가
    public long getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }

    public long getAccessTokenValidity() {
        return ACCESS_TOKEN_VALIDITY;
    }
}
