package com.project.flow.connector.provider;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.common.exception.InternalServerError;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter for executing GitHub integrations.
 * Supports creating Pull Requests.
 */
@Component
public class GithubConnectorAdapter implements IConnectorAdapter {

    private final RestTemplate restTemplate;

    public GithubConnectorAdapter() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Constructor allowing mock injecting for testing.
     */
    public GithubConnectorAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public Map<String, Object> execute(String action, Map<String, Object> inputs, Map<String, String> decryptedSecrets) {
        if (!"create_pull_request".equalsIgnoreCase(action)) {
            throw new InvalidRequestException("Unsupported GitHub action: " + action, null);
        }

        // 1. Retrieve & validate credentials
        String token = decryptedSecrets.get("token");
        if (token == null || token.isBlank()) {
            throw new InvalidRequestException("GitHub credentials token is missing", null);
        }

        // 2. Retrieve & validate inputs
        String repository = (String) inputs.get("repository"); // Format: owner/repo
        String title = (String) inputs.get("title");
        String head = (String) inputs.get("head");
        String base = (String) inputs.get("base");
        String body = (String) inputs.get("body");

        if (repository == null || repository.isBlank()) {
            throw new InvalidRequestException("Repository input is required", null);
        }
        if (title == null || title.isBlank()) {
            throw new InvalidRequestException("Title input is required", null);
        }
        if (head == null || head.isBlank()) {
            throw new InvalidRequestException("Head branch input is required", null);
        }
        if (base == null || base.isBlank()) {
            throw new InvalidRequestException("Base branch input is required", null);
        }

        // 3. Invoke GitHub API: POST /repos/{owner}/{repo}/pulls
        String url = "https://api.github.com/repos/" + repository + "/pulls";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.set("User-Agent", "Flow-Automation-Platform");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("head", head);
        requestBody.put("base", base);
        if (body != null) {
            requestBody.put("body", body);
        }

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("pr_url", response.getBody().get("html_url"));
                result.put("pr_number", response.getBody().get("number"));
                result.put("pr_id", response.getBody().get("id"));
                result.put("state", response.getBody().get("state"));
                return result;
            } else {
                throw new InternalServerError("GitHub API returned non-success response: " + response.getStatusCode(), Optional.empty());
            }
        } catch (HttpClientErrorException e) {
            throw new InvalidRequestException("GitHub API Client Error: " + e.getResponseBodyAsString(), e, Optional.empty());
        } catch (HttpServerErrorException e) {
            throw new InternalServerError("GitHub API Server Error: " + e.getResponseBodyAsString(), e, Optional.empty());
        } catch (Exception e) {
            throw new InternalServerError("Failed to communicate with GitHub API: " + e.getMessage(), e, Optional.empty());
        }
    }
}
