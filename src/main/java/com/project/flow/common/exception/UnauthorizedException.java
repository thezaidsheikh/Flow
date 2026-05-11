package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {

    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "Unauthorized", Optional.empty());
    }

    public UnauthorizedException(String message, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message, details);
    }

    public UnauthorizedException(String message, Throwable cause, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, message, cause, details);
    }
}
