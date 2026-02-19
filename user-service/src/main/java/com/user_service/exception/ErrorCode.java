package com.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    USER_NOT_FOUND("USER_NOT_FOUND", "User not found with id %s", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "Current password is invalid", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_MISMATCH("NEW_PASSWORD_MISMATCH", "The new password must be different from the current password", HttpStatus.CONFLICT),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
    MISMATCH_PASSWORD("MISMATCH_PASSWORD", "Passwords do not match", HttpStatus.BAD_REQUEST),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACTIVATION_TOKEN("ACTIVATION_TOKEN", "Activation token should not be null or empty", HttpStatus.BAD_REQUEST),
    LOGIN_ATTEMPT("LOGIN_ATTEMPT", "You have been temporarily locked due to too many failed login attempts", HttpStatus.CONFLICT),
    PASSWORD_RESET_TOKEN("PASSWORD_RESET_TOKEN", "Password Reset token should not be null or empty", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(
            final String code,
            final String defaultMessage,
            final HttpStatus status
    ) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }
}
