package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends AppException {

    public InvalidRequestException() {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Request is invalid", null);
    }

    public InvalidRequestException(String message, Optional<Object> details) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, details);
    }

    public InvalidRequestException(String message, Throwable cause, Optional<Object> details) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message, cause, details);
    }
}
