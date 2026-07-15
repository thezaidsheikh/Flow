package com.project.flow.common.exception;

import com.project.flow.common.enums.ErrorCode;
import com.project.flow.common.response.ApiErrorResponse;
import com.project.flow.common.response.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex) {
        String requestId = UUID.randomUUID().toString();

        log.warn(
            "Handled AppException: requestId={}, path={}, status={}, errorCode={}, message={}",
            requestId,
            request.getRequestURI(),
            ex.getStatus().value(),
            ex.getErrorCode().name(),
            ex.getMessage(),
            ex
        );

        ApiErrorResponse response = ApiErrorResponse.builder()
            .success(false)
            .statusCode(ex.getStatus().value())
            .message(ex.getMessage())
            .error(new ErrorDetails(ex.getErrorCode().name(), ex.getDetails()))
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .requestId(requestId)
            .build();

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", ErrorCode.VALIDATION_ERROR, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: path={}", request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body", ErrorCode.VALIDATION_ERROR, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", ErrorCode.RESOURCE_NOT_FOUND, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", ErrorCode.METHOD_NOT_ALLOWED, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        String requestId = UUID.randomUUID().toString();

        log.error("Unhandled exception: requestId={}, path={}, message={}", requestId, request.getRequestURI(), ex.getMessage(), ex);

        ApiErrorResponse response = ApiErrorResponse.builder()
            .success(false)
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Something went wrong")
            .error(new ErrorDetails(ErrorCode.INTERNAL_SERVER_ERROR.name(), null))
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .requestId(requestId)
            .build();

        return ResponseEntity.internalServerError().body(response);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message, ErrorCode errorCode, Object details) {
        ApiErrorResponse response = ApiErrorResponse.builder()
            .success(false)
            .statusCode(status.value())
            .message(message)
            .error(new ErrorDetails(errorCode.name(), details))
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .requestId(UUID.randomUUID().toString())
            .build();

        return ResponseEntity.status(status).body(response);
    }
}
