package com.api_gateway.security;

import com.api_gateway.dto.UserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.List;

@Component
public class JwtTokenHelper {

    private final Key secret_key;

    public JwtTokenHelper(@Value("${jwt.secret.key}") String secretKey) {
        byte[] bytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.secret_key = Keys.hmacShaKeyFor(bytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) secret_key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secret_key)
                .build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean isRefreshToken(String token) {
        final Claims claims = extractClaims(token);
        return "REFRESH_TOKEN".equals(claims.get("token_type"));
    }

    public UserResponse extractPayloadFromToken(String token) {
        Claims claims = extractClaims(token);
        ObjectMapper mapper = new ObjectMapper();

        List<String> roles = mapper.convertValue(
                claims.get("roles"), new TypeReference<List<String>>() {
                }
        );

        return UserResponse
                .builder()
                .userId(claims.get("userId").toString())
                .email(claims.getSubject())
                .role(roles)
                .build();
    }
}
