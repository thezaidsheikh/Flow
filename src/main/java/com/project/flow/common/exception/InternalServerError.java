package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class InternalServerError extends AppException {

    public InternalServerError() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "Internal Server Error", null);
    }

    public InternalServerError(String message, Optional<Object> details) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message, details);
    }

    public InternalServerError(String message, Throwable cause, Optional<Object> details) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, message, cause, details);
    }
}
