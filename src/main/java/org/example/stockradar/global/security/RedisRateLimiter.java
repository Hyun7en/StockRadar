package org.example.stockradar.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 고정 윈도우 카운터 기반의 단순 rate limiter.
 * SMS 발송, 아이디 찾기, 로그인 실패 등 남용 가능한 엔드포인트 보호용.
 */
@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    /** 호출 1회를 기록하고, 윈도우 내 허용 횟수 이내면 true */
    public boolean tryAcquire(String key, int limit, Duration window) {
        String redisKey = "RATE:" + key;
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, window);
        }
        return count != null && count <= limit;
    }

    /** 기록 없이 현재 횟수가 한도를 넘었는지만 확인 */
    public boolean isBlocked(String key, int limit) {
        String value = redisTemplate.opsForValue().get("RATE:" + key);
        return value != null && Long.parseLong(value) >= limit;
    }

    public void reset(String key) {
        redisTemplate.delete("RATE:" + key);
    }
}
