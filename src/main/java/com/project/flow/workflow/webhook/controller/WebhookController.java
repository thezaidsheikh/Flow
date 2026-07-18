package com.project.flow.workflow.webhook.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.workflow.webhook.service.WebhookRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookRegistrationService webhookRegistrationService;

    @PostMapping("/{path}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<WorkflowRunDetailResponse> handleWebhook(
        @PathVariable String path,
        HttpServletRequest request
    ) throws IOException {
        byte[] rawBody = request.getInputStream().readAllBytes();
        String signature = request.getHeader("X-Hub-Signature-256");

        WorkflowRunDetailResponse response = webhookRegistrationService.executeWebhook(path, rawBody, signature);

        return ApiResponse.<WorkflowRunDetailResponse>builder()
            .success(true)
            .statusCode(202)
            .message("Webhook received and workflow executed")
            .data(response)
            .build();
    }
}
