package com.user_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    private int failedAttemptCount;
    private long lastFailedAt;
    private boolean isActive;
}
