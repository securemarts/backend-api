package com.shopper.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter {

    private final int maxAttemptsPerMinute;
    private final Cache<String, AtomicInteger> attemptsByKey;

    public LoginRateLimiter(@Value("${app.auth.rate-limit-login-per-minute:10}") int maxAttemptsPerMinute) {
        this.maxAttemptsPerMinute = maxAttemptsPerMinute;
        this.attemptsByKey = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(key -> new AtomicInteger(0));
    }

    public boolean allowRequest(String key) {
        AtomicInteger count = attemptsByKey.get(key, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= maxAttemptsPerMinute;
    }

    public void reset(String key) {
        attemptsByKey.invalidate(key);
    }
}
