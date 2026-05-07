package com.project.flow.common.exception;

import java.util.Optional;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final transient Object details;

    protected AppException(HttpStatus status, ErrorCode errorCode, String message, Optional<Object> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }
}
