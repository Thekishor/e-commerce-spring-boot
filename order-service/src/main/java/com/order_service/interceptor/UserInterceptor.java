package com.order_service.interceptor;

import com.order_service.dto.UserInfo;
import com.order_service.exception.ErrorCode;
import com.order_service.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class UserInterceptor extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String userId = request.getHeader("X-User-Id");
            String userEmail = request.getHeader("X-User-Email");
            String roles = request.getHeader("X-User-Roles");

            log.info("User headers information: {} {} {}", userId, userEmail, roles);

            if (userId == null || userEmail == null || roles.isEmpty()) {
                log.warn("Missing user headers in request");
                throw new BusinessException(ErrorCode.USERINFO_FOUND);
            }
            UserInfo userInfo =
                    new UserInfo(userId, userEmail, roles);
            UserContext.setUserInfo(userInfo);
            log.info("UserContext Thread Local information: {} {} {}",
                    UserContext.getUserId(), UserContext.getUserEmail(), UserContext.getUserRole());
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
