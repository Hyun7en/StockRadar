package org.example.stockradar.feature.auth.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * SESSION_ID(랜덤 UUID, HttpOnly 쿠키) → memberId 매핑을 Redis에 저장.
 * SESSION_ID 자체가 refresh token 역할을 하며, 로그아웃 시 삭제로 즉시 무효화됨.
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveSession(String sessionId, String memberId, long validityMillis) {
        redisTemplate.opsForValue().set("SESSION:" + sessionId, memberId, Duration.ofMillis(validityMillis));
    }

    public String getMemberId(String sessionId) {
        return redisTemplate.opsForValue().get("SESSION:" + sessionId);
    }

    public void deleteRefreshToken(String sessionId) {
        redisTemplate.delete("SESSION:" + sessionId);
    }
}
