package com.api_gateway.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserResponse {

    private String userId;
    private String username;
    private String email;
    private String role;
    private Boolean isActive;
}
