package com.project.flow.workflow.webhook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.credential.service.GetCredentialService;
import com.project.flow.execution.orchestrator.WorkflowExecutionOrchestrator;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import com.project.flow.workflow.repository.WorkflowRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookRegistrationService {

    private static final String WEBHOOK_SECRET_KEY = "secret";
    private static final String WEBHOOK_TRIGGER_SUBTYPE = "WEBHOOK";
    private static final String GITHUB_PUSH_TRIGGER_SUBTYPE = "GITHUB_PUSH";
    private static final String CREDENTIAL_ID_KEY = "credentialId";
    private static final String REPO_KEY = "repo";
    private static final String EVENTS_KEY = "events";
    private static final String SOURCE_BRANCH_KEY = "sourceBranch";
    private static final String SOURCE_BRANCH_FALLBACK_KEY = "branch";

    private final WebhookRegistrationRepository webhookRegistrationRepository;
    private final GetWorkflowService getWorkflowService;
    private final WorkflowExecutionOrchestrator orchestrator;
    private final GitHubWebhookService gitHubWebhookService;
    private final GetCredentialService getCredentialService;
    private final WorkflowRepository workflowRepository;

    @Value("${app.webhook.base-url:}")
    private String webhookBaseUrl;

    @Transactional
    public void registerWebhooks(String workflowId, String userId, WorkflowVersion version) {
        List<Node> webhookTriggerNodes = version.getNodes().stream()
            .filter(node -> node.getType() == NodeType.TRIGGER
                && (WEBHOOK_TRIGGER_SUBTYPE.equalsIgnoreCase(node.getSubType())
                    || GITHUB_PUSH_TRIGGER_SUBTYPE.equalsIgnoreCase(node.getSubType())))
            .toList();

        if (webhookTriggerNodes.isEmpty()) {
            return;
        }

        deactivateExistingRegistrations(workflowId);

        for (Node triggerNode : webhookTriggerNodes) {
            String path = generateWebhookPath(workflowId, triggerNode.getId());
            String secret = extractSecretFromConfig(triggerNode.getConfig());

            WebhookRegistration registration = WebhookRegistration.builder()
                .path(path)
                .userId(userId)
                .workflowId(workflowId)
                .workflowVersionId(version.getId())
                .nodeId(triggerNode.getId())
                .secret(secret)
                .active(true)
                .build();

            if (GITHUB_PUSH_TRIGGER_SUBTYPE.equalsIgnoreCase(triggerNode.getSubType())) {
                registerGitHubWebhook(registration, triggerNode, userId, path);
            }

            webhookRegistrationRepository.save(registration);
            log.info("Registered webhook for workflow={}, user={}, path={}", workflowId, userId, path);
        }
    }

    @Transactional
    public void deactivateWebhooks(String workflowId) {
        List<WebhookRegistration> registrations = webhookRegistrationRepository.findByWorkflowId(workflowId);
        for (WebhookRegistration registration : registrations) {
            deleteGitHubWebhookIfPresent(registration);
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

        if (!matchesBranchFilter(triggerData, registration)) {
            log.info("Webhook ignored: branch filter mismatch for path={}, ref={}", path, triggerData.get("ref"));
            return null;
        }

        enrichTriggerDataWithBranches(triggerData, registration);

        WorkflowVersion version = getWorkflowService.loadGraph(
            getWorkflowService.getPublishedVersionById(registration.getWorkflowId())
        );

        var run = orchestrator.execute(
            registration.getWorkflowId(),
            version,
            registration.getUserId(),
            triggerData,
            Map.of(),
            "WEBHOOK"
        );

        String workflowName = workflowRepository
            .findByIdAndUserId(run.getWorkflowId(), run.getUserId())
            .map(Workflow::getName)
            .orElse("Unknown Workflow");

        return new WorkflowRunDetailResponse(
            run.getId(),
            run.getWorkflowId(),
            workflowName,
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

    private void registerGitHubWebhook(WebhookRegistration registration, Node triggerNode, String userId, String path) {
        Map<String, Object> config = normalizeConfig(triggerNode.getConfig());
        String credentialId = requireStringConfig(config, CREDENTIAL_ID_KEY,
            "GitHub push trigger node must have a credentialId in config");
        String repo = requireStringConfig(config, REPO_KEY,
            "GitHub push trigger node must have a repo (owner/repo) in config");

        registration.setCredentialId(credentialId);
        String sourceBranch = extractStringConfig(config, SOURCE_BRANCH_KEY);
        if (sourceBranch == null) {
            sourceBranch = extractStringConfig(config, SOURCE_BRANCH_FALLBACK_KEY);
        }
        registration.setSourceBranch(sourceBranch);

        if (webhookBaseUrl == null || webhookBaseUrl.isBlank() || isLocalhost(webhookBaseUrl)) {
            log.info("Skipping GitHub webhook creation for repo={}: base URL is not publicly reachable (localhost or unset). "
                + "Configure WEBHOOK_BASE_URL to a publicly accessible URL for automatic webhook creation.", repo);
            return;
        }

        Map<String, String> decryptedSecrets = getCredentialService.getDecryptedSecrets(credentialId, userId);
        String token = decryptedSecrets.get("token");
        if (token == null || token.isBlank()) {
            throw new InvalidRequestException("GitHub token is missing from credentials", null);
        }

        String callbackUrl = webhookBaseUrl + "/hooks/" + path;
        String secret = extractSecretFromConfig(triggerNode.getConfig());
        List<String> events = extractEvents(config);
        long hookId = gitHubWebhookService.createWebhook(repo, token, callbackUrl, secret, events);

        registration.setGithubHookId(hookId);
        registration.setGithubHookUrl("https://api.github.com/repos/" + repo + "/hooks/" + hookId);
    }

    private boolean isLocalhost(String url) {
        String lower = url.toLowerCase();
        return lower.contains("localhost") || lower.contains("127.0.0.1") || lower.contains("0.0.0.0");
    }

    private void deleteGitHubWebhookIfPresent(WebhookRegistration registration) {
        if (registration.getGithubHookId() == null || registration.getCredentialId() == null) {
            return;
        }
        try {
            Map<String, String> decryptedSecrets = getCredentialService.getDecryptedSecrets(
                registration.getCredentialId(), registration.getUserId());
            String token = decryptedSecrets.get("token");
            if (token != null && !token.isBlank()) {
                String repo = extractRepoFromHookUrl(registration.getGithubHookUrl());
                gitHubWebhookService.deleteWebhook(repo, token, registration.getGithubHookId());
            }
        } catch (Exception e) {
            log.warn("Failed to delete GitHub webhook hookId={} for registration={}: {}",
                registration.getGithubHookId(), registration.getId(), e.getMessage());
        }
    }

    private void deactivateExistingRegistrations(String workflowId) {
        List<WebhookRegistration> existingRegistrations = webhookRegistrationRepository.findByWorkflowId(workflowId);
        for (WebhookRegistration existing : existingRegistrations) {
            deleteGitHubWebhookIfPresent(existing);
            existing.setActive(false);
            webhookRegistrationRepository.save(existing);
        }
    }

    private boolean matchesBranchFilter(Map<String, Object> triggerData, WebhookRegistration registration) {
        String sourceBranch = registration.getSourceBranch();
        if (sourceBranch == null || sourceBranch.isBlank()) {
            return true;
        }

        String ref = (String) triggerData.get("ref");
        if (ref == null || ref.isBlank()) {
            return true;
        }

        String branch = ref.replaceFirst("^refs/heads/", "");
        return matchesGlob(sourceBranch, branch);
    }

    private void enrichTriggerDataWithBranches(Map<String, Object> triggerData, WebhookRegistration registration) {
        String ref = (String) triggerData.get("ref");
        if (ref != null && !ref.isBlank()) {
            String sourceBranch = ref.replaceFirst("^refs/heads/", "");
            triggerData.put("source_branch", sourceBranch);
        }
        if (registration.getTargetBranch() != null && !registration.getTargetBranch().isBlank()) {
            triggerData.put("target_branch", registration.getTargetBranch());
        }
    }

    private boolean matchesGlob(String pattern, String value) {
        String regex = pattern.replace("*", ".*").replace("?", ".");
        return value.matches("^" + regex + "$");
    }

    private List<String> extractEvents(Map<String, Object> config) {
        Object eventsObj = config.get(EVENTS_KEY);
        if (eventsObj instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of("push");
    }

    private String extractRepoFromHookUrl(String githubHookUrl) {
        if (githubHookUrl == null) {
            throw new InvalidRequestException("GitHub hook URL is missing", null);
        }
        String prefix = "https://api.github.com/repos/";
        String suffix = "/hooks/";
        int start = githubHookUrl.indexOf(prefix);
        int end = githubHookUrl.indexOf(suffix);
        if (start == -1 || end == -1) {
            throw new InvalidRequestException("Cannot parse repo from GitHub hook URL: " + githubHookUrl, null);
        }
        return githubHookUrl.substring(start + prefix.length(), end);
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

    @SuppressWarnings("unchecked")
    private String extractStringConfig(Object config, String key) {
        if (config instanceof Map<?, ?> map) {
            Object value = map.get(key);
            if (value instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return null;
    }

    private Map<String, Object> normalizeConfig(Object config) {
        if (config instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new java.util.LinkedHashMap<>();
            map.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            return normalized;
        }
        return Map.of();
    }

    private String requireStringConfig(Map<String, Object> config, String key, String message) {
        Object value = config.get(key);
        if (value instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new InvalidRequestException(message, null);
    }
}
