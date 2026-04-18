package com.user_service.auditing;

import com.user_service.security.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ApplicationAuditAware implements AuditorAware<UUID> {

    private static final UUID SYSTEM_ID =
            UUID.fromString("11b986ac-330e-49ac-a251-e86dfa1ce026");

    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken
        ) {
            return Optional.of(SYSTEM_ID);
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return Optional.ofNullable(Objects.requireNonNull(userDetails).getUserId());
    }
}
