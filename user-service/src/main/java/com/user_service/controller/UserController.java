package com.user_service.controller;

import com.user_service.dto.*;
import com.user_service.service.UserService;
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

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordModel resetPasswordModel
    ) {
        userService.generatePasswordResetToken(resetPasswordModel.getEmail());
        return new ResponseEntity<>(Map.of(
                "message", "Password reset link sent to your email"
        ), HttpStatus.OK);
    }

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

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
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
    public ResponseEntity<UserResponse> findByUserId(@PathVariable("userId") String userId) {
        UserResponse user = userService.findByUserId(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
