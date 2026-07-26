package com.project.flow.workflow.webhook.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.workflow.webhook.service.WebhookRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Webhooks", description = "External webhook trigger endpoints for workflow automation (no auth required)")
public class WebhookController {

    private final WebhookRegistrationService webhookRegistrationService;

    @PostMapping("/{path}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "Receive webhook",
        description = """
            Receive an incoming webhook from an external service and trigger the associated workflow.
            No JWT authentication is required; instead, an optional HMAC-SHA256 signature is validated
            if a shared secret was configured on the trigger node.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Webhook received and workflow executed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Webhook not found or inactive"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid webhook signature")
    })
    public ApiResponse<WorkflowRunDetailResponse> handleWebhook(
        @Parameter(description = "24-character hex path assigned when the webhook was registered") @PathVariable String path,
        HttpServletRequest request
    ) throws IOException {
        byte[] rawBody = request.getInputStream().readAllBytes();
        String signature = request.getHeader("X-Hub-Signature-256");

        WorkflowRunDetailResponse response = webhookRegistrationService.executeWebhook(path, rawBody, signature);

        if (response == null) {
            return ApiResponse.<WorkflowRunDetailResponse>builder()
                .success(true)
                .statusCode(202)
                .message("Webhook received but ignored due to branch filter")
                .data(null)
                .build();
        }

        return ApiResponse.<WorkflowRunDetailResponse>builder()
            .success(true)
            .statusCode(202)
            .message("Webhook received and workflow executed")
            .data(response)
            .build();
    }
}
