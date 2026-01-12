package com.user_service.service;

import com.user_service.security.CustomUserDetails;
import com.user_service.security.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ROLE = "role";

    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpirationTime;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    private final CustomUserDetailsService userDetailsService;

    public String generateAccessToken(final String username, CustomUserDetails customUserDetails) {
        final Map<String, Object> claims = new HashMap<>();
        List<String> roles = customUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        claims.put(ROLE, roles);
        claims.put(TOKEN_TYPE, "ACCESS_TOKEN");
        return buildToken(username, claims, accessTokenExpirationTime);
    }

    public String generateRefreshToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        return buildToken(username, claims, refreshTokenExpirationTime);
    }

    private String buildToken(String email, Map<String, Object> claims, long expiration) {
        return Jwts
                .builder()
                .header()
                .add("typ", "JWT")
                .and()
                .subject(email)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] bytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    private Claims extractClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Jwt token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String refreshAccessToken(final String refreshToken) {
        final Claims claims = extractClaims(refreshToken);
        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) {
            throw new RuntimeException("Invalid token type");
        }

        if (isTokenExpired(refreshToken)) {
            throw new RuntimeException("Refresh token expired");
        }

        String username = extractUsername(refreshToken);
        final CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        return generateAccessToken(claims.getSubject(), userDetails);
    }
}
