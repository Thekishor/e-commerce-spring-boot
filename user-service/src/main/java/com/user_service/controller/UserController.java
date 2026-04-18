package com.user_service.controller;

import com.user_service.dto.*;
import com.user_service.service.UserService;
import common.events.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final String MESSAGE = "message";
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

        AuthResponse authResponse = userService.generateJwtToken(authRequest);

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
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        Map<String, Object> objectMap = userService.refreshToken(refreshRequest);
        return new ResponseEntity<>(objectMap, HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordModel passwordModel
    ) {
        if (!userService.checkIfValidOldPassword(passwordModel.getOldPassword())) {
            return new ResponseEntity<>(Map.of(
                    MESSAGE, "Invalid Old Password"
            ), HttpStatus.BAD_REQUEST);
        }
        if (userService.passwordMatches(passwordModel.getNewPassword())) {
            return new ResponseEntity<>(Map.of(
                    MESSAGE, "New password must be different from the previous password"
            ), HttpStatus.CONFLICT);
        }
        if (!passwordModel.getNewPassword().equals(passwordModel.getConfirmPassword())) {
            return new ResponseEntity<>(Map.of(
                    MESSAGE, "New Password and Confirm New Password do not match"
            ), HttpStatus.BAD_REQUEST);
        }
        userService.changedPassword(passwordModel.getNewPassword());
        return new ResponseEntity<>(Map.of(
                MESSAGE, "Password Change Successfully"
        ), HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordModel resetPasswordModel
    ) {
        userService.generatePasswordResetToken(resetPasswordModel.getEmail());
        return new ResponseEntity<>(Map.of(
                MESSAGE, "Password reset link sent to your email"
        ), HttpStatus.OK);
    }

    @Hidden
    @PostMapping("/savePassword")
    public ResponseEntity<Map<String, String>> savePassword(
            @RequestParam("token") String token,
            @Valid @RequestBody SavePassword savePassword
    ) {
        boolean success = userService.validatePasswordResetToken(token, savePassword);
        if (success) {
            return new ResponseEntity<>(Map.of(
                    MESSAGE, "Password Change Successfully"
            ), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of(
                MESSAGE, "Reset Password link expired or invalid"
        ), HttpStatus.BAD_REQUEST);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User deleted successfully");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUser() {
        List<UserResponse> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findByUserId(@PathVariable("userId") UUID userId) {
        UserResponse user = userService.findByUserId(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
