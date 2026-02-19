package com.product_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND),
    MORE_PRODUCT_NOT_FOUND("MORE_PRODUCT_NOT_FOUND", "One or More products does not exists", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "Insufficient stock", HttpStatus.BAD_REQUEST),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_FOUND("CATEGORY_ALREADY_FOUND", "Category name already exists", HttpStatus.CONFLICT);

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
