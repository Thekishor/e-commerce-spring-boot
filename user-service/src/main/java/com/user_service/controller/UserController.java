package com.user_service.controller;

import com.user_service.dto.*;
import com.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(
            description = "user register",
            summary = "Create user account/ New user signup",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error",
                            responseCode = "500"
                    )
            }
    )
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> createUser(
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse userResponse = userService.createUser(userRequest);
        log.info("Current register user information: {} {}", userResponse, LocalDateTime.now());
        return new ResponseEntity<>(Map.of(
                "message", "Please check your email to verify your account"
        ), HttpStatus.CREATED);
    }

    @Hidden
    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        try {
            boolean isActivated = userService.activatedProfile(token);
            if (isActivated) {
                return ResponseEntity.ok("Profile activated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.GONE)
                        .body("Email verification link expired. Please sign up again");
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Operation(
            description = "user login",
            summary = "user login rate limit",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Invalid username or password",
                            responseCode = "401"
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            if (!userService.isAccountActive(authRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "message", "User not found with email"
                        ));
            }
            Map<String, Object> response = userService.generateJwtToken(authRequest);
            return ResponseEntity.ok(response);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message", exception.getMessage()
                    ));
        }
    }

    @Operation(
            description = "logout user",
            summary = "user logout",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error",
                            responseCode = "401"
                    )
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        userService.logout();
        return ResponseEntity.ok("You have been signed out");
    }

    @Operation(
            description = "refresh token",
            summary = "refresh token for new access token"
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        Map<String, Object> objectMap = userService.refreshToken(refreshRequest);
        return new ResponseEntity<>(objectMap, HttpStatus.OK);
    }

    @Operation(
            description = "change password",
            summary = "change user password",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error",
                            responseCode = "401"
                    )
            }
    )
    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody PasswordModel passwordModel
    ) {
        if (!userService.checkIfValidOldPassword(passwordModel.getOldPassword())) {
            return new ResponseEntity<>(Map.of(
                    "message", "Invalid Old Password"
            ), HttpStatus.BAD_REQUEST);
        }
        if (userService.passwordMatches(passwordModel.getNewPassword())) {
            return new ResponseEntity<>(Map.of(
                    "message", "New password must be different from the previous password"
            ), HttpStatus.CONFLICT);
        }
        if (!passwordModel.getNewPassword().equals(passwordModel.getConfirmPassword())) {
            return new ResponseEntity<>(Map.of(
                    "message", "New Password and Confirm New Password do not match"
            ), HttpStatus.BAD_REQUEST);
        }
        userService.changedPassword(passwordModel.getNewPassword());
        return new ResponseEntity<>(Map.of(
                "message", "Password Change Successfully"
        ), HttpStatus.OK);
    }

    @Operation(
            description = "reset password",
            summary = "reset user password",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error",
                            responseCode = "401"
                    )
            }
    )
    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordModel resetPasswordModel
    ) {
        userService.generatePasswordResetToken(resetPasswordModel.getEmail());
        return new ResponseEntity<>(Map.of(
                "message", "Password reset link sent to your email"
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
                    "message", "Password Change Successfully"
            ), HttpStatus.OK);
        }
        return new ResponseEntity<>(Map.of(
                "message", "Reset Password link expired or invalid"
        ), HttpStatus.BAD_REQUEST);
    }

    @Operation(
            description = "delete user",
            summary = "delete the user account",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Internal Server Error",
                            responseCode = "401"
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User deleted successfully");
    }

    @Operation(
            description = "Get all user",
            summary = "only admin should be able to access users info",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized",
                            responseCode = "403"
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAllUser() {
        List<UserResponse> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(
            description = "Get by user",
            summary = "Get user info or details by user id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized",
                            responseCode = "403"
                    )
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findByUserId(@PathVariable("userId") String userId) {
        UserResponse user = userService.findByUserId(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
