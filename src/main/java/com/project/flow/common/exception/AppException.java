package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode errorCode;
    private final transient Object details;

    protected AppException(HttpStatus status, ErrorCode errorCode, String message, Optional<Object> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        Optional<Object> safeDetails = Objects.requireNonNullElse(details, Optional.empty());
        this.details = safeDetails.orElse(null);
    }

    protected AppException(HttpStatus status, ErrorCode errorCode, String message, Throwable cause, Optional<Object> details) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        Optional<Object> safeDetails = Objects.requireNonNullElse(details, Optional.empty());
        this.details = safeDetails.orElse(null);
    }
}
