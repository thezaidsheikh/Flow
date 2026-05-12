package com.project.flow.common.response;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.stereotype.Component;

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
