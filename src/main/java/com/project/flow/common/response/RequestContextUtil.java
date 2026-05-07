package com.project.flow.common.response;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RequestContextUtil {

    public String getRequestId(HttpServletRequest request) {

        Object requestId = request.getAttribute("requestId");

        if (requestId != null) {
            return requestId.toString();
        }

        return UUID.randomUUID().toString();
    }
}
