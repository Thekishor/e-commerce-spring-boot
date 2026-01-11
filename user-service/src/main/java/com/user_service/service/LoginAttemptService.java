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

    private static final String MAIN_KEY = "LOGIN_ATTEMPTS:";
    private static final int MAX_ATTEMPT = 5;
    private static final long LOCK_TIME = TimeUnit.MINUTES.toSeconds(20);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void loginSucceeded(String username) {
        String userKey = MAIN_KEY + username;
        redisTemplate.delete(userKey);
    }

    public void loginFailed(String username) {
        String userKey = MAIN_KEY + username;
        Object object = redisTemplate.opsForValue().get(userKey);

        LoginAttempt loginAttempt =
                objectMapper.convertValue(object, LoginAttempt.class);

        if (loginAttempt == null) {
            loginAttempt = LoginAttempt
                    .builder()
                    .key(userKey)
                    .failedAttemptCount(1)
                    .build();
        } else {
            loginAttempt.setFailedAttemptCount(loginAttempt.getFailedAttemptCount() + 1);
            redisTemplate.opsForValue().set(userKey, loginAttempt);
        }
        loginAttempt.setFailedAt(System.currentTimeMillis());
        loginAttempt.setStatus("ACTIVE");

        if (loginAttempt.getFailedAttemptCount() >= MAX_ATTEMPT) {
            loginAttempt.setStatus("BLOCKED");
            loginAttempt.setFailedAt(System.currentTimeMillis());
            redisTemplate.opsForValue()
                    .set(userKey, loginAttempt, LOCK_TIME, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(userKey, loginAttempt);
        }
    }

    public boolean isBlocked(String username) {
        String userKey = MAIN_KEY + username;
        log.info("User key: {}", userKey);
        Object object = redisTemplate.opsForValue().get(userKey);
        log.info("Object from redis in memory: {}", object);

        if (object == null) {
            return false;
        }
        LoginAttempt loginAttempt =
                objectMapper.convertValue(object, LoginAttempt.class);
        log.info("LoginAttempt information of users: {}", loginAttempt);

        return loginAttempt.getFailedAttemptCount() >= MAX_ATTEMPT;
    }
}
