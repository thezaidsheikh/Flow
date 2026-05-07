package com.project.flow.common.exception;

import com.project.flow.common.response.ApiErrorResponse;
import com.project.flow.common.response.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final HttpServletRequest request;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(
            AppException ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                ex.getStatus().value(),
                ex.getMessage(),
                new ErrorDetails(
                        ex.getErrorCode().name(),
                        ex.getDetails()),
                OffsetDateTime.now(),
                request.getRequestURI(),
                UUID.randomUUID().toString());

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                400,
                "Validation failed",
                new ErrorDetails(
                        ErrorCode.VALIDATION_ERROR.name(),
                        errors),
                OffsetDateTime.now(),
                request.getRequestURI(),
                UUID.randomUUID().toString());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex) {

        ApiErrorResponse response = new ApiErrorResponse(
                false,
                500,
                "Something went wrong",
                new ErrorDetails(
                        ErrorCode.INTERNAL_SERVER_ERROR.name(),
                        null),
                OffsetDateTime.now(),
                request.getRequestURI(),
                UUID.randomUUID().toString());

        return ResponseEntity.internalServerError().body(response);
    }
}