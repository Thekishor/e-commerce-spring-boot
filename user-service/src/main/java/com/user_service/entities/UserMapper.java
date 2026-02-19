package com.user_service.entities;

import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;

public class UserMapper {

    public static UserResponse mapUserEntityToUserResponse(User savedUser) {
        return UserResponse.builder()
                .username(savedUser.getUsername())
                .isActive(savedUser.getIsActive())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    public static User mapUserRequestToUserEntity(UserRequest userRequest) {
        return User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .build();
    }
}
