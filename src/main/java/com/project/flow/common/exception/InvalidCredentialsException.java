package com.project.flow.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Optional;

public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid email or password", null);
    }

    public InvalidCredentialsException(String message, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, message, details);
    }
}
