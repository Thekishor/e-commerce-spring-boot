package com.user_service.exception;

public class PasswordConflictException extends RuntimeException{

    public PasswordConflictException(String msg) {
        super(msg);
    }
}
