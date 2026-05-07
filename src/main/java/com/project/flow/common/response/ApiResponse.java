package com.project.flow.common.response;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        int statusCode,
        String message,
        T data,
        Object meta,
        OffsetDateTime timestamp,
        String path,
        String requestId) {
}
