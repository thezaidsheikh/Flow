package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import java.util.Optional;
import org.springframework.http.HttpStatus;

public class ResourceConflictException extends AppException {

    public ResourceConflictException() {
        super(HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT, "The resource already exists.", null);
    }

    public ResourceConflictException(String message, Optional<Object> details) {
        super(HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT, message, details);
    }
}
