package com.user_service.service;

import com.user_service.dto.*;
import com.user_service.entities.PasswordResetToken;
import com.user_service.entities.User;
import com.user_service.entities.VerificationToken;
import com.user_service.exception.BadCredentialsException;
import com.user_service.exception.PasswordConflictException;
import com.user_service.exception.UserAlreadyExistsException;
import com.user_service.exception.UserNotFoundException;
import com.user_service.repository.JwtTokenRepository;
import com.user_service.repository.PasswordResetTokenRepository;
import com.user_service.repository.UserRepository;
import com.user_service.repository.VerificationTokenRepository;
import com.user_service.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Value("${backend_url}")
    private String backend_url;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtservice;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final JwtTokenRepository jwtTokenRepository;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.error("User already exists with email: {}", userRequest.getEmail());
            throw new UserAlreadyExistsException("User already found with email");
        }
        log.info("User email {}", userRequest.getEmail());
        User user = mapUserRequestToUserEntity(userRequest);
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        User savedUser = userRepository.save(user);
        VerificationToken verificationToken = VerificationToken.builder()
                .user(savedUser)
                .activationToken(UUID.randomUUID().toString())
                .activationTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();
        VerificationToken savedToken = verificationTokenRepository.save(verificationToken);
        log.info("User verification information: {}", savedToken);
        String verificationLink = backend_url + "/activate?token=" + savedToken.getActivationToken();
        try {
            emailService.sendEmailVerificationLink(savedUser.getEmail(), verificationLink, savedUser.getUsername());
            log.info("Sending email verification link to user");
        } catch (Exception exception) {
            log.error("Exception occurred while sending verification link to user: {}", exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
        log.debug("User registered successfully {}", savedUser);
        return mapUserEntityToUserResponse(savedUser);
    }

    private UserResponse mapUserEntityToUserResponse(User savedUser) {
        return UserResponse.builder()
                .username(savedUser.getUsername())
                .isActive(savedUser.getIsActive())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    private User mapUserRequestToUserEntity(UserRequest userRequest) {
        return User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .build();
    }

    @Override
    public UserResponse findByUserId(String userId) {
        log.info("User with Id {}", userId);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id"));
        log.debug("User find with id {}", user);
        return mapUserEntityToUserResponse(user);
    }

    @Override
    public List<UserResponse> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapUserEntityToUserResponse).toList();
    }

    @Transactional
    @Override
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id"));
        userRepository.delete(user);
    }

    @Override
    public boolean activatedProfile(String token) {
        if (token == null || token.isEmpty()) {
            log.warn("Activation token should not be null or empty");
            throw new RuntimeException("Activation token should not be null or empty");
        }
        VerificationToken verificationToken = verificationTokenRepository.findByActivationToken(token);
        User user = userRepository.findById(verificationToken.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id"));
        if (verificationToken.getActivationTokenExpiry().isAfter(LocalDateTime.now())) {
            user.setIsActive(true);
            user.setEmailVerified(true);
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);
            return true;
        } else {
            verificationTokenRepository.delete(verificationToken);
            userRepository.delete(user);
            return false;
        }
    }

    @Override
    public boolean isAccountActive(String email) {
        return userRepository.findByEmail(email)
                .map(User::getIsActive).orElse(false);
    }

    @Override
    public Map<String, Object> generateJwtToken(AuthRequest authRequest) {

        if (loginAttemptService.isBlocked(authRequest.getEmail())) {
            log.error("User have been temporarily locked due to too many login attempts: {}", authRequest.getEmail());
            throw new BadCredentialsException("You have been temporarily locked due to too many failed login attempts.");
        }

        try {
            final Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authenticate);

            log.info("Authentication user info from dao auth provider: {}", authenticate.getPrincipal());

            final CustomUserDetails userDetails =
                    (CustomUserDetails) authenticate.getPrincipal();
            log.info("User information from db: {}", userDetails);

            if (userDetails == null) {
                log.info("User not found with email: {}", authRequest.getEmail());
                throw new RuntimeException("Authentication failed");
            }

            loginAttemptService.loginSucceeded(userDetails.getUsername());
            log.info("Login successful: {}", userDetails.getUsername());

            final String accessToken = jwtservice.generateAccessToken(userDetails.getUsername(), userDetails);
            final String refreshToken = jwtservice.generateRefreshToken(userDetails.getUsername());

            //store tokens inside redis
            jwtTokenRepository.storeToken(
                    userDetails.getUsername(),
                    accessToken,
                    refreshToken
            );

            return Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshToken,
                    "authorities", userDetails.getAuthorities(),
                    "generatedAt", Instant.now().toString()
            );
        } catch (Exception exception) {
            loginAttemptService.loginFailed(authRequest.getEmail());
            log.info("Invalid username or password. {}", authRequest.getEmail());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public void logout() {
        //Get current auth user from security context holder
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            log.warn("User not found with email");
            throw new RuntimeException("Authentication failed");
        }

        //Remove all tokens for this user
        jwtTokenRepository.removeAllTokens(authentication.getName());
    }

    @Override
    public Map<String, Object> refreshToken(RefreshRequest refreshRequest) {
        final String accessToken = jwtservice.refreshAccessToken(refreshRequest.getRefreshToken());
        return Map.of(
                "access_token", accessToken,
                "refresh_token", refreshRequest.getRefreshToken(),
                "generatedAt", Instant.now().toString()
        );
    }

    @Override
    public boolean checkIfValidOldPassword(String oldPassword) {
        User user = getCurrentLoggedInUser();
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public boolean passwordMatches(String newPassword) {
        User user = getCurrentLoggedInUser();
        return passwordEncoder.matches(newPassword, user.getPassword());
    }

    @Override
    public void changedPassword(String newPassword) {
        User user = getCurrentLoggedInUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email"));
        PasswordResetToken passwordResetToken = PasswordResetToken
                .builder().user(user)
                .token(UUID.randomUUID().toString())
                .tokenExpiry(LocalDateTime.now().plusHours(1))
                .build();
        PasswordResetToken savedPasswordResetToken =
                passwordResetTokenRepository.save(passwordResetToken);
        String passwordResetLink = backend_url + "/savePassword?token=" + savedPasswordResetToken.getToken();
        try {
            emailService.sendPasswordResetLink(user.getEmail(), passwordResetLink, user.getUsername());
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public boolean validatePasswordResetToken(String token, SavePassword savePassword) {
        if (token == null || token.isEmpty()) {
            log.warn("Password Reset token should not be null or empty");
            throw new RuntimeException("Password Reset token should not be null or empty");
        }
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        User user = userRepository.findById(passwordResetToken.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id"));

        if (passwordResetToken.getTokenExpiry().isAfter(LocalDateTime.now())) {
            if (!savePassword.getNewPassword().equals(savePassword.getConfirmPassword())) {
                throw new PasswordConflictException("New password and confirm password do not match");
            }
            user.setPassword(passwordEncoder.encode(savePassword.getNewPassword()));
            userRepository.save(user);
            passwordResetTokenRepository.delete(passwordResetToken);
            return true;
        }
        return false;
    }

    private User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found with email"));
    }
}
