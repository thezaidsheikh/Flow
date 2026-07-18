package com.project.flow.common.response;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private static final int HTTP_OK = 200;
    private static final int HTTP_CREATED = 201;
    private static final String[] EXCLUDED_PATHS = {
        "/v3/api-docs",
        "/swagger-ui"
    };

    private final HttpServletRequest request;
    private final RequestContextUtil requestContextUtil;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip wrapping if already wrapped
        if (ApiResponse.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // Skip if annotation present
        if (returnType.hasMethodAnnotation(NoWrapResponse.class) || returnType.getContainingClass().isAnnotationPresent(NoWrapResponse.class)) {
            return false;
        }

        // Skip springdoc/swagger paths
        String path = request.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (path != null && path.contains(excluded)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        ServerHttpRequest serverHttpRequest,
        ServerHttpResponse serverHttpResponse
    ) {
        return ApiResponse.builder()
            .success(true)
            .statusCode(resolveSuccessStatusCode())
            .message("Request successful")
            .data(body)
            .meta(null)
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .requestId(requestContextUtil.getRequestId(request))
            .build();
    }

    private int resolveSuccessStatusCode() {
        String method = request.getMethod();
        if (method == null) {
            return HTTP_OK;
        }

        return switch (method.toUpperCase()) {
            case "GET", "PUT", "PATCH", "DELETE" -> HTTP_OK;
            case "POST" -> HTTP_CREATED;
            default -> HTTP_OK;
        };
    }
}
