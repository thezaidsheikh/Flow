package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends AppException {

    public TokenExpiredException() {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED, "Token expired", Optional.empty());
    }

    public TokenExpiredException(String message, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED, message, details);
    }

    public TokenExpiredException(String message, Throwable cause, Optional<Object> details) {
        super(HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED, message, cause, details);
    }
}
