package com.api_gateway.security;

import com.api_gateway.dto.UserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Component
public class JwtTokenHelper {

    private final Key secret_key;

    public JwtTokenHelper(@Value("${jwt.secret.key}") String secretKey) {
        byte[] bytes = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
        this.secret_key = Keys.hmacShaKeyFor(bytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) secret_key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secret_key)
                .build()
                .parseSignedClaims(token).getPayload();
    }

    public UserResponse extractPayloadFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return UserResponse.builder()
                .userId((String) claims.get("userId"))
                .email(claims.getSubject())
                .role((String) claims.get("role"))
                .build();
    }
}
