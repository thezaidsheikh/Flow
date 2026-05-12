package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class ResourceNotFound extends AppException {

    public ResourceNotFound() {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, "Resource not found", null);
    }

    public ResourceNotFound(String message, Optional<Object> details) {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message, details);
    }

    public ResourceNotFound(String message, Throwable cause, Optional<Object> details) {
        super(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message, cause, details);
    }
}
