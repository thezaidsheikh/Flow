package com.project.flow.workflow.webhook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.execution.orchestrator.WorkflowExecutionOrchestrator;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.webhook.domain.WebhookRegistration;
import com.project.flow.workflow.webhook.repository.WebhookRegistrationRepository;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookRegistrationService {

    private static final String WEBHOOK_SECRET_KEY = "secret";
    private static final String SYSTEM_USER_ID = "system";
    private static final String WEBHOOK_TRIGGER_SUBTYPE = "WEBHOOK";
    private static final String GITHUB_PUSH_TRIGGER_SUBTYPE = "GITHUB_PUSH";

    private final WebhookRegistrationRepository webhookRegistrationRepository;
    private final GetWorkflowService getWorkflowService;
    private final WorkflowExecutionOrchestrator orchestrator;

    @Transactional
    public void registerWebhooks(String workflowId, WorkflowVersion version) {
        List<Node> webhookTriggerNodes = version.getNodes().stream()
            .filter(node -> node.getType() == NodeType.TRIGGER
                && (WEBHOOK_TRIGGER_SUBTYPE.equalsIgnoreCase(node.getSubType())
                    || GITHUB_PUSH_TRIGGER_SUBTYPE.equalsIgnoreCase(node.getSubType())))
            .toList();

        if (webhookTriggerNodes.isEmpty()) {
            return;
        }

        List<WebhookRegistration> existingRegistrations = webhookRegistrationRepository.findByWorkflowId(workflowId);
        for (WebhookRegistration existing : existingRegistrations) {
            existing.setActive(false);
            webhookRegistrationRepository.save(existing);
        }

        for (Node triggerNode : webhookTriggerNodes) {
            String path = generateWebhookPath(workflowId, triggerNode.getId());
            String secret = extractSecretFromConfig(triggerNode.getConfig());

            WebhookRegistration registration = WebhookRegistration.builder()
                .path(path)
                .workflowId(workflowId)
                .workflowVersionId(version.getId())
                .nodeId(triggerNode.getId())
                .secret(secret)
                .active(true)
                .build();

            webhookRegistrationRepository.save(registration);
            log.info("Registered webhook for workflow={}, path={}", workflowId, path);
        }
    }

    @Transactional
    public void deactivateWebhooks(String workflowId) {
        List<WebhookRegistration> registrations = webhookRegistrationRepository.findByWorkflowId(workflowId);
        for (WebhookRegistration registration : registrations) {
            registration.setActive(false);
            webhookRegistrationRepository.save(registration);
        }
        log.info("Deactivated webhooks for workflow={}", workflowId);
    }

    @Transactional
    public WorkflowRunDetailResponse executeWebhook(String path, byte[] rawBody, String signatureHeader) {
        WebhookRegistration registration = webhookRegistrationRepository.findByPathAndActiveTrue(path)
            .orElseThrow(() -> new ResourceNotFound("Webhook not found or inactive", Optional.empty()));

        verifySignature(registration.getSecret(), rawBody, signatureHeader);

        Map<String, Object> triggerData = parsePayload(rawBody);

        WorkflowVersion version = getWorkflowService.loadGraph(
            getWorkflowService.getPublishedVersionById(registration.getWorkflowId())
        );

        var run = orchestrator.execute(
            registration.getWorkflowId(),
            version,
            SYSTEM_USER_ID,
            triggerData,
            Map.of(),
            "WEBHOOK"
        );

        return new WorkflowRunDetailResponse(
            run.getId(),
            run.getWorkflowId(),
            run.getWorkflowVersionId(),
            run.getStatus().name(),
            run.getTriggerType(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getInputPayload(),
            run.getOutputPayload(),
            run.getErrorMessage()
        );
    }

    private void verifySignature(String secret, byte[] rawBody, String signatureHeader) {
        if (secret == null || secret.isBlank()) {
            return;
        }

        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new InvalidRequestException("Missing webhook signature", Optional.empty());
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expectedSignature = mac.doFinal(rawBody);
            String expectedHex = HexFormat.of().formatHex(expectedSignature);

            if (!MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8)
            )) {
                throw new InvalidRequestException("Invalid webhook signature", Optional.empty());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new InvalidRequestException("Failed to verify webhook signature", Optional.empty());
        }
    }

    private Map<String, Object> parsePayload(byte[] rawBody) {
        try {
            if (rawBody == null || rawBody.length == 0) {
                return Map.of();
            }
            ObjectMapper plainMapper = new ObjectMapper();
            return plainMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse webhook payload: {}", e.getMessage());
            return Map.of();
        }
    }

    private String generateWebhookPath(String workflowId, String nodeId) {
        try {
            String input = workflowId + ":" + nodeId;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractSecretFromConfig(Object config) {
        if (config instanceof Map<?, ?> map) {
            Object secret = map.get(WEBHOOK_SECRET_KEY);
            if (secret instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }
}
