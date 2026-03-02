package com.user_service;

import com.user_service.dto.UserRequest;
import com.user_service.dto.UserResponse;
import com.user_service.entities.User;
import com.user_service.entities.VerificationToken;
import com.user_service.exception.BusinessException;
import com.user_service.exception.ErrorCode;
import com.user_service.mapper.UserMapper;
import com.user_service.repository.UserRepository;
import com.user_service.repository.VerificationTokenRepository;
import com.user_service.service.EmailService;
import com.user_service.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User service implementation test")
class UserServiceApplicationTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private UserResponse userResponse;
    private User user;
    private User savedUser;
    private VerificationToken verificationToken;
    private VerificationToken savedVerificationToken;
    private String verificationLink;

    @BeforeEach
    void setUp() {
        //Given user request
        userRequest = UserRequest.builder()
                .username("Kishor Pandey")
                .email("kishorpandey829@gmail.com")
                .password("Kishor3344@@##")
                .build();

        // User information
        user = User.builder()
                .userId("0b091eae127c4bdd9ea568c710f9ce2c")
                .username("Kishor Pandey")
                .email("kishorpandey829@gmail.com")
                .password(passwordEncoder.encode("Kishor3344@@##"))
                .build();

        // Saved user information
        savedUser = User.builder()
                .userId("0b091eae127c4bdd9ea568c710f9ce2c")
                .username("Kishor Pandey")
                .email("kishorpandey829@gmail.com")
                .password(passwordEncoder.encode("Kishor3344@@##"))
                .createdAt(LocalDateTime.now())
                .emailVerified(true)
                .role(List.of("ADMIN"))
                .build();

        // Verification token
        verificationToken = VerificationToken.builder()
                .user(savedUser)
                .activationToken("3139d6aa-0fe0-4e34-8faf-bc9194d69b5c")
                .activationTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        // saved Verification token
        savedVerificationToken = VerificationToken.builder()
                .user(savedUser)
                .activationToken("3139d6aa-0fe0-4e34-8faf-bc9194d69b5c")
                .activationTokenExpiry(LocalDateTime.now().plusHours(1))
                .build();

        // Send verification link to user
        verificationLink = "http://localhost:9000/api/user" + "/activate?token="
                + savedVerificationToken.getActivationToken();

        // Return user response
        userResponse = UserResponse.builder()
                .username("Kishor Pandey")
                .email("kishorpandey829@gmail.com")
                .role(List.of("ADMIN"))
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Create user-service test")
    class CreateUserServiceTests {

        @Test
        @DisplayName("Should register user successfully when valid request exists")
        void shouldRegisterUserSuccessfully() {

            // Given
            when(userRepository.existsByEmail(userRequest.getEmail()))
                    .thenReturn(false);
            when(userMapper.mapUserRequestToUserEntity(userRequest)).thenReturn(user);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(savedVerificationToken);
            doThrow(new BusinessException(ErrorCode.INTERNAL_EXCEPTION))
                    .when(emailService)
                    .sendEmailVerificationLink(savedUser.getEmail(), verificationLink, savedUser.getUsername());
            when(userMapper.mapUserEntityToUserResponse(savedUser)).thenReturn(userResponse);

            // When
            userResponse =
                    userService.createUser(userRequest);

            // Then
            assertNotNull(userResponse);
            assertEquals("kishorpandey829@gmail.com", userResponse.getEmail());
            assertEquals("kishor Pandey", userResponse.getUsername());
            assertEquals("ADMIN", userResponse.getRole().toString());

            // Verify
            verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
            verify(userRepository, times(1)).save(user);
        }
    }

}
