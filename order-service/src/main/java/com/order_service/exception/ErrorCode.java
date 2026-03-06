package com.order_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED_EXCEPTION("UNAUTHORIZED_EXCEPTION", "Authorization header missing or invalid", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found with userId %s", HttpStatus.NOT_FOUND),
    USERINFO_FOUND("USERINFO_FOUND", "User info not found in request header", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", "Order not found with id %s", HttpStatus.NOT_FOUND);

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
