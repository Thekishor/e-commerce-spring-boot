package com.api_gateway.security;

import com.api_gateway.dto.UserResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenHelper jwtTokenHelper;

    @Override
    public Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain
    ) {
        String requestToken
                = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = null;

        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            token = requestToken.substring(7);

            if (jwtTokenHelper.isRefreshToken(token)) {
                log.warn("Refresh token not allowed to access resources");
                throw new RuntimeException("Refresh token not allowed to access resources");
            }

            if (jwtTokenHelper.validateToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserResponse userResponse =
                        jwtTokenHelper.extractPayloadFromToken(token);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userResponse,
                                null,
                                userResponse.getRole().stream().map(SimpleGrantedAuthority::new).toList()
                        );
                authenticationToken.setDetails(exchange);
                SecurityContext context = new SecurityContextImpl(authenticationToken);

                //set the principal header in the request
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", userResponse.getEmail())
                        .header("X-User-Id", userResponse.getUserId())
                        .header("X-User-Roles", userResponse.getRole().toString())
                        .build();

                //create a new server web exchange with modified request
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange).contextWrite(
                        ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
            } else {
                log.error("TOKEN IS MALFORMED OR EXPIRED");
            }
        } else {
            log.error("TOKEN NOT FOUND");
        }
        return chain.filter(exchange);
    }
}
