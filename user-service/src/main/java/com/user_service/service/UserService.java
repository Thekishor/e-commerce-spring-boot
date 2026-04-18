package com.user_service.service;

import com.user_service.dto.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse findByUserId(UUID userId);

    List<UserResponse> findAllUsers();

    void deleteUser(UUID id);

    boolean activatedProfile(String token);

    AuthResponse login(AuthRequest authRequest);

    void logout();

    Map<String, Object> refreshToken(RefreshRequest refreshRequest);

    boolean checkIfValidOldPassword(String oldPassword);

    boolean passwordMatches(String newPassword);

    void changedPassword(String newPassword);

    void generatePasswordResetToken(String email);

    boolean validatePasswordResetToken(String token, SavePassword savePassword);

    List<UserResponse> isEmailVerified();
}
