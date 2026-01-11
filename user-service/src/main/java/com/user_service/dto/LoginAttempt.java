package com.user_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    private String key;
    private int failedAttemptCount;
    private long failedAt;
    private String status;
}
