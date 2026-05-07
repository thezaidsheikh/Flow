package com.project.flow.common.exception;

import java.util.Optional;

import org.springframework.http.HttpStatus;

public class InternalServerError extends AppException {
    public InternalServerError() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", null);
    }

    public InternalServerError(String message, Optional<Object> details) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message, details);
    }
}
