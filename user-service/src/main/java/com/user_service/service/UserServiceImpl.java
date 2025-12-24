package com.user_service.service;

import com.user_service.dto.AuthRequest;
import com.user_service.dto.SavePassword;
import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;
import com.user_service.entities.PasswordResetToken;
import com.user_service.entities.User;
import com.user_service.entities.VerificationToken;
import com.user_service.exception.PasswordConflictException;
import com.user_service.exception.UserAlreadyExistsException;
import com.user_service.exception.UserNotFoundException;
import com.user_service.repository.PasswordResetTokenRepository;
import com.user_service.repository.UserRepository;
import com.user_service.repository.VerificationTokenRepository;
import com.user_service.utils.JwtUtils;
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
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.error("User already exists with email:");
            throw new UserAlreadyExistsException("User already found with email:" + userRequest.getEmail());
        }
        log.info("User Request: {}", userRequest);
        User user = mapUserRequestToUserEntity(userRequest);
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        User savedUser = userRepository.save(user);
        VerificationToken verificationToken = VerificationToken.builder()
                .user(savedUser)
                .activationToken(UUID.randomUUID().toString())
                .activationTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();
        VerificationToken savedToken = verificationTokenRepository.save(verificationToken);
        String verificationLink = backend_url + "/activate?token=" + savedToken.getActivationToken();
        try {
            emailService.sendEmailVerificationLink(savedUser.getEmail(), verificationLink, savedUser.getUsername());
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
        return mapUserEntityToUserResponse(savedUser);
    }

    private UserResponse mapUserEntityToUserResponse(User savedUser) {
        return UserResponse.builder()
                .userId(savedUser.getUserId())
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
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id"));
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
        try {
            authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            User user =
                    userRepository.findByEmail(authRequest.getEmail())
                            .orElseThrow(() -> new UserNotFoundException("User not found with id"));
            String token = jwtUtils.generateToken(authRequest.getEmail(), user);
            return Map.of(
                    "token", token,
                    "expiration", "Your token will expire in 1 hours"
            );
        } catch (Exception exception) {
            throw new RuntimeException("Invalid email or password");
        }
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
