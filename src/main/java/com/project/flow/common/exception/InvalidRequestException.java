package com.project.flow.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Optional;

public class InvalidRequestException extends AppException {

    public InvalidRequestException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Request is invalid", null);
    }

    public InvalidRequestException(String message, Optional<Object> details) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, details);
    }
}
