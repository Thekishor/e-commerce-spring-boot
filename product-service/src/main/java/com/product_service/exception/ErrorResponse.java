package com.product_service.exception;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ErrorResponse {

    private String message;
    private String code;
    private List<ValidationError> validationErrors;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @Builder
    public static class ValidationError {

        private String field;
        private String code;
        private String message;
    }
}
