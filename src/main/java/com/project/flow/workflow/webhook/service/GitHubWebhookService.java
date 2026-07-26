package com.project.flow.workflow.webhook.service;

import com.project.flow.common.exception.InvalidRequestException;

import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Service to manage webhooks on GitHub repositories via the GitHub REST API.
 * Supports creating, updating, and deleting repository webhooks.
 */
@Service
@Slf4j
public class GitHubWebhookService {

    private static final String GITHUB_API_BASE = "https://api.github.com/repos/";

    private final RestTemplate restTemplate;

    public GitHubWebhookService() {
        this.restTemplate = new RestTemplate();
    }

    public GitHubWebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Creates a webhook on a GitHub repository.
     *
     * @param repo        repository in "owner/repo" format
     * @param token       GitHub personal access token
     * @param callbackUrl the URL GitHub will send events to
     * @param secret      optional shared secret for HMAC-SHA256 signature validation
     * @param events      list of GitHub events to subscribe to (e.g. ["push"])
     * @return the created webhook's ID
     */
    @SuppressWarnings("unchecked")
    public long createWebhook(String repo, String token, String callbackUrl, String secret, List<String> events) {
        String url = GITHUB_API_BASE + repo + "/hooks";

        Map<String, Object> config = new HashMap<>();
        config.put("url", callbackUrl);
        config.put("content_type", "json");
        config.put("insecure_ssl", "0");
        if (secret != null && !secret.isBlank()) {
            config.put("secret", secret);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", "web");
        body.put("active", true);
        body.put("events", events);
        body.put("config", config);

        HttpHeaders headers = createHeaders(token);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object hookId = response.getBody().get("id");
                if (hookId instanceof Number number) {
                    log.info("Created GitHub webhook on repo={}, hookId={}", repo, number.longValue());
                    return number.longValue();
                }
                throw new InvalidRequestException("GitHub API returned unexpected hook ID format", null);
            }
            throw new InvalidRequestException("GitHub API returned non-success status: " + response.getStatusCode(), null);
        } catch (HttpClientErrorException e) {
            throw new InvalidRequestException("Failed to create GitHub webhook: " + e.getResponseBodyAsString(), e, Optional.empty());
        }
    }

    /**
     * Deletes a webhook from a GitHub repository.
     *
     * @param repo   repository in "owner/repo" format
     * @param token  GitHub personal access token
     * @param hookId the webhook ID to delete
     */
    public void deleteWebhook(String repo, String token, long hookId) {
        String url = GITHUB_API_BASE + repo + "/hooks/" + hookId;
        HttpHeaders headers = createHeaders(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            log.info("Deleted GitHub webhook from repo={}, hookId={}", repo, hookId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("GitHub webhook hookId={} not found on repo={}, may already be deleted", hookId, repo);
                return;
            }
            throw new InvalidRequestException("Failed to delete GitHub webhook: " + e.getResponseBodyAsString(), e, Optional.empty());
        }
    }

    /**
     * Updates an existing webhook on a GitHub repository.
     *
     * @param repo        repository in "owner/repo" format
     * @param token       GitHub personal access token
     * @param hookId      the webhook ID to update
     * @param callbackUrl the new callback URL
     * @param secret      optional shared secret for HMAC-SHA256 signature validation
     * @param events      list of GitHub events to subscribe to
     */
    @SuppressWarnings("unchecked")
    public void updateWebhook(String repo, String token, long hookId, String callbackUrl, String secret, List<String> events) {
        String url = GITHUB_API_BASE + repo + "/hooks/" + hookId;

        Map<String, Object> config = new HashMap<>();
        config.put("url", callbackUrl);
        config.put("content_type", "json");
        config.put("insecure_ssl", "0");
        if (secret != null && !secret.isBlank()) {
            config.put("secret", secret);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("active", true);
        body.put("events", events);
        body.put("config", config);

        HttpHeaders headers = createHeaders(token);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PATCH, request, Map.class);
            log.info("Updated GitHub webhook on repo={}, hookId={}", repo, hookId);
        } catch (HttpClientErrorException e) {
            throw new InvalidRequestException("Failed to update GitHub webhook: " + e.getResponseBodyAsString(), e, Optional.empty());
        }
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.set("User-Agent", "Flow-Automation-Platform");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
