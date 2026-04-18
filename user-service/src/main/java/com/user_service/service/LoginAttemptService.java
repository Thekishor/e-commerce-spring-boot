package com.user_service.service;

import com.user_service.dto.LoginAttempt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private static final String LOGIN_ATTEMPTS_PREFIX = "LOGIN_ATTEMPTS:";
    private static final int MAX_LOGIN_ATTEMPT = 5;
    private static final long LOCK_TIME = TimeUnit.MINUTES.toSeconds(60);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void loginSucceeded(String username) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + username;
        redisTemplate.delete(userKey);
    }

    public void loginFailed(String username) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + username;

        LoginAttempt loginAttempt = objectMapper
                .convertValue(redisTemplate.opsForValue().get(userKey), LoginAttempt.class);

        if (loginAttempt == null) {
            loginAttempt = LoginAttempt.builder()
                    .failedAttemptCount(1)
                    .isActive(true)
                    .lastFailedAt(System.currentTimeMillis())
                    .build();
        } else {
            loginAttempt.setFailedAttemptCount(loginAttempt.getFailedAttemptCount() + 1);
            loginAttempt.setLastFailedAt(System.currentTimeMillis());
        }

        if (loginAttempt.getFailedAttemptCount() > MAX_LOGIN_ATTEMPT) {
            loginAttempt.setActive(false);
            redisTemplate.opsForValue()
                    .set(userKey, loginAttempt, LOCK_TIME, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(userKey, loginAttempt);
        }
    }

    public boolean isBlocked(String username) {
        String userKey = LOGIN_ATTEMPTS_PREFIX + username;

        LoginAttempt loginAttempt = objectMapper
                .convertValue(redisTemplate.opsForValue().get(userKey), LoginAttempt.class);

        if (loginAttempt == null) {
            return false;
        }

        return loginAttempt.getFailedAttemptCount() > MAX_LOGIN_ATTEMPT;
    }
}
