package com.user_service.repository;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class JwtTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    //key prefixes for token storage
    private static final String ACCESS_TOKEN_KEY_PREFIX = "user:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "user:refresh:";

    //key prefixes for token blacklisting
    private static final String ACCESS_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_BLACKLIST_PREFIX = "blacklist:refresh:";

    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpirationTime;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    public void storeToken(
            String username,
            String accessToken,
            String refreshToken
    ) {
        //store access token
        String accessKey = ACCESS_TOKEN_KEY_PREFIX + username;
        storeToken(accessKey, accessToken, accessTokenExpirationTime);

        //store refresh token
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + username;
        storeToken(refreshKey, refreshToken, refreshTokenExpirationTime);
    }

    private void storeToken(
            String key,
            String token,
            long expiration
    ) {
        redisTemplate.opsForValue().set(key, token, expiration, TimeUnit.MILLISECONDS);
    }

    /*
     * Retrieve the access token for a user
     */
    public String getAccessToken(String username) {
        String accessKey = ACCESS_TOKEN_KEY_PREFIX + username;
        return getToken(accessKey);
    }

    /*
     * Retrieve the refresh token for a user
     */
    public String getRefreshToken(String username) {
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + username;
        return getToken(refreshKey);
    }

    private String getToken(String key) {
        ObjectMapper mapper = new ObjectMapper();
        Object object = redisTemplate.opsForValue().get(key);
        return mapper.convertValue(object, String.class);
    }

    /*
     * Remove all tokens for a user (complete logout)
     */
    public void removeAllTokens(String username) {
        String accessToken = getAccessToken(username);
        String refreshToken = getRefreshToken(username);

        String accessKey = ACCESS_TOKEN_KEY_PREFIX + username;
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + username;

        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        if (accessToken != null) {
            String accessBlackListKey = ACCESS_BLACKLIST_PREFIX + accessToken;
            blackListToken(accessBlackListKey, accessTokenExpirationTime);
        }

        if (refreshToken != null) {
            String refreshBlackListKey = REFRESH_BLACKLIST_PREFIX + refreshToken;
            blackListToken(refreshBlackListKey, refreshTokenExpirationTime);
        }
    }

    public boolean isAccessTokenBlockListed(String token) {
        String key = ACCESS_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    public boolean isRefreshTokenBlockListed(String token) {
        String key = REFRESH_BLACKLIST_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    private void blackListToken(String blackListKey, long expiration) {
        redisTemplate.opsForValue().set(blackListKey, "blacklisted", expiration, TimeUnit.MILLISECONDS);
    }

    public void removeAccessToken(@Nullable String username) {
        String accessToken = getAccessToken(username);
        String accessKey = ACCESS_TOKEN_KEY_PREFIX + username;
        redisTemplate.delete(accessKey);

        // Blacklist the access token
        String accessBlackListKey = ACCESS_BLACKLIST_PREFIX + accessToken;
        blackListToken(accessBlackListKey, accessTokenExpirationTime);
    }
}
