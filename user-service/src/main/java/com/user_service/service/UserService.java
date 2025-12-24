package com.user_service.service;

import com.user_service.dto.AuthRequest;
import com.user_service.dto.SavePassword;
import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    UserResponse findByUserId(String userId);

    List<UserResponse> findAllUsers();

    void deleteUser(Integer id);

    boolean activatedProfile(String token);

    boolean isAccountActive(String email);

    Map<String, Object> generateJwtToken(AuthRequest authRequest);

    boolean checkIfValidOldPassword(String oldPassword);

    boolean passwordMatches(String newPassword);

    void changedPassword(String newPassword);

    void generatePasswordResetToken(String email);

    boolean validatePasswordResetToken(String token, SavePassword savePassword);
}
