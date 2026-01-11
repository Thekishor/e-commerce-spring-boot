package com.user_service.exception;

import lombok.Getter;

@Getter
public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException(String msg) {
        super(msg);
    }
}
