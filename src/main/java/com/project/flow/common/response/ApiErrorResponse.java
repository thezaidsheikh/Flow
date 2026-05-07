package com.project.flow.common.response;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        boolean success,
        int statusCode,
        String message,
        ErrorDetails error,
        OffsetDateTime timestamp,
        String path,
        String requestId) {
}
