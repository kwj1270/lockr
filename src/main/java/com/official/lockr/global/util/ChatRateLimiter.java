package com.official.lockr.global.util;

import com.official.lockr.global.exception.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ChatRateLimiter {

    private static final String KEY_PREFIX = "chat:rate:";
    private static final long MAX_REQUESTS = 30;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public ChatRateLimiter(final StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimit(final String userId) {
        final String key = KEY_PREFIX + userId;
        final Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW);
        }
        if (count != null && count > MAX_REQUESTS) {
            throw new RateLimitExceededException("메시지 전송 속도 제한을 초과했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
