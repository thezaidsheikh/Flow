package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, "Invalid email or password", null);
    }

    public InvalidCredentialsException(String message, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_CREDENTIALS, message, details);
    }
}
