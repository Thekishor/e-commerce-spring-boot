package com.user_service.controller;

import com.user_service.dto.*;
import com.user_service.service.UserService;
import common.events.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse userResponse =
                userService.createUser(userRequest);

        log.info("User Response : {}", userResponse);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .message("Please check your email to verify your account")
                        .data(userResponse)
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activateProfile(
            @RequestParam String token) {

        boolean isActivated =
                userService.activatedProfile(token);

        if (isActivated) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<String>builder()
                            .message("Profile activated successfully")
                            .success(true)
                            .status(200)
                            .build());

        } else {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(ApiResponse.<String>builder()
                            .message("Email verification link expired. Please sign up again")
                            .success(false)
                            .status(410)
                            .build()
                    );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest authRequest
    ) {

        AuthResponse authResponse = userService.login(authRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<AuthResponse>builder()
                        .message("User logged in successfully")
                        .data(authResponse)
                        .success(true)
                        .status(200)
                        .build());
    }

    @GetMapping("/verified")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getEmailVerifiedUser() {

        List<UserResponse> userResponses = userService.isEmailVerified();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<List<UserResponse>>builder()
                        .message("Verified Users")
                        .data(userResponses)
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {

        userService.logout();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<String>builder()
                        .message("You have been signed out")
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest refreshRequest
    ) {
        AuthResponse authResponse = userService.refreshToken(refreshRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<AuthResponse>builder()
                        .message("Token generated successfully")
                        .data(authResponse)
                        .status(200)
                        .success(true)
                        .build()
                );
    }

    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody PasswordModel passwordModel
    ) {
        if (!userService.checkIfValidOldPassword(passwordModel.getOldPassword())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>builder()
                            .message("Invalid Old Password")
                            .success(false)
                            .status(400)
                            .build()
                    );
        }
        if (userService.passwordMatches(passwordModel.getNewPassword())) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<String>builder()
                            .message("New password must be different from the previous password")
                            .success(false)
                            .status(409)
                            .build()
                    );
        }
        if (!passwordModel.getNewPassword().equals(passwordModel.getConfirmPassword())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>builder()
                            .message("New Password and Confirm New Password do not match")
                            .success(false)
                            .status(400)
                            .build()
                    );
        }
        userService.changedPassword(passwordModel.getNewPassword());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<String>builder()
                        .message("Password Change Successfully")
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordModel resetPasswordModel
    ) {
        userService.generatePasswordResetToken(resetPasswordModel.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<String>builder()
                        .message("Password reset link sent to your email")
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @PostMapping("/savePassword")
    public ResponseEntity<ApiResponse<String>> savePassword(
            @RequestParam("token") String token,
            @Valid @RequestBody SavePassword savePassword
    ) {
        boolean success = userService
                .validatePasswordResetToken(token, savePassword);

        if (success) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.<String>builder()
                            .message("Password Change Successfully")
                            .success(true)
                            .status(200)
                            .build()
                    );
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<String>builder()
                        .message("Reset Password link expired or invalid")
                        .success(false)
                        .status(400)
                        .build()
                );
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable("id") UUID id) {

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<String>builder()
                        .message("User deleted successfully")
                        .success(true)
                        .status(200)
                        .build());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> findAllUser() {

        List<UserResponse> userResponses = userService.findAllUsers();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<List<UserResponse>>builder()
                        .message("Users fetched successfully")
                        .data(userResponses)
                        .success(true)
                        .status(200)
                        .build()
                );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> findByUserId(
            @PathVariable("userId") UUID userId) {

        UserResponse userResponse = userService.findByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<UserResponse>builder()
                        .message("User fetched successfully")
                        .data(userResponse)
                        .success(true)
                        .status(200)
                        .build()
                );
    }
}