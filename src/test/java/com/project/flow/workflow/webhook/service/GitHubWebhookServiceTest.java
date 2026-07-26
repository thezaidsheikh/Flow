package com.project.flow.workflow.webhook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.project.flow.common.exception.InvalidRequestException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class GitHubWebhookServiceTest {

    private RestTemplate restTemplate;
    private GitHubWebhookService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        service = new GitHubWebhookService(restTemplate);
    }

    @Test
    void shouldCreateWebhookSuccessfully() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", 123456L);
        responseBody.put("url", "https://api.github.com/repos/owner/repo/hooks/123456");
        responseBody.put("active", true);

        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.CREATED));

        long hookId = service.createWebhook(
            "owner/repo", "ghp_token", "https://example.com/hooks/abc", "secret123", List.of("push")
        );

        assertEquals(123456L, hookId);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("https://api.github.com/repos/owner/repo/hooks"),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(Map.class)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertNotNull(body);
        assertEquals("web", body.get("name"));
        assertEquals(true, body.get("active"));
        assertEquals(List.of("push"), body.get("events"));

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) body.get("config");
        assertNotNull(config);
        assertEquals("https://example.com/hooks/abc", config.get("url"));
        assertEquals("json", config.get("content_type"));
        assertEquals("secret123", config.get("secret"));
    }

    @Test
    void shouldCreateWebhookWithoutSecret() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("id", 789L);

        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.CREATED));

        long hookId = service.createWebhook(
            "owner/repo", "ghp_token", "https://example.com/hooks/abc", null, List.of("push")
        );

        assertEquals(789L, hookId);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("https://api.github.com/repos/owner/repo/hooks"),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(Map.class)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) body.get("config");
        assertNull(config.get("secret"));
    }

    @Test
    void shouldThrowOnNon2xxResponse() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        assertThrows(InvalidRequestException.class, () ->
            service.createWebhook("owner/repo", "ghp_token", "https://example.com/hooks/abc", null, List.of("push"))
        );
    }

    @Test
    void shouldThrowOnGitHubClientError() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden", "Insufficient permissions".getBytes(), null));

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () ->
            service.createWebhook("owner/repo", "ghp_token", "https://example.com/hooks/abc", null, List.of("push"))
        );
        assertTrue(ex.getMessage().contains("Failed to create GitHub webhook"));
    }

    @Test
    void shouldDeleteWebhookSuccessfully() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.NO_CONTENT));

        assertDoesNotThrow(() ->
            service.deleteWebhook("owner/repo", "ghp_token", 123456L)
        );

        verify(restTemplate).exchange(
            eq("https://api.github.com/repos/owner/repo/hooks/123456"),
            eq(HttpMethod.DELETE),
            any(HttpEntity.class),
            eq(Void.class)
        );
    }

    @Test
    void shouldNotThrowWhenWebhookAlreadyDeleted() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        assertDoesNotThrow(() ->
            service.deleteWebhook("owner/repo", "ghp_token", 123456L)
        );
    }

    @Test
    void shouldThrowOnDeleteFailure() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden"));

        assertThrows(InvalidRequestException.class, () ->
            service.deleteWebhook("owner/repo", "ghp_token", 123456L)
        );
    }

    @Test
    void shouldUpdateWebhookSuccessfully() {
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<>(new HashMap<>(), HttpStatus.OK));

        assertDoesNotThrow(() ->
            service.updateWebhook("owner/repo", "ghp_token", 123456L, "https://new-url.com/hooks/xyz", "new-secret", List.of("push"))
        );

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("https://api.github.com/repos/owner/repo/hooks/123456"),
            eq(HttpMethod.PATCH),
            captor.capture(),
            eq(Map.class)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) captor.getValue().getBody();
        assertNotNull(body);
        assertEquals(true, body.get("active"));

        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) body.get("config");
        assertNotNull(config);
        assertEquals("https://new-url.com/hooks/xyz", config.get("url"));
        assertEquals("new-secret", config.get("secret"));
    }

    @Test
    void shouldSetCorrectHeaders() {
        Map<String, Object> responseBody = Map.of("id", 1L);
        when(restTemplate.exchange(
            anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)
        )).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.CREATED));

        service.createWebhook("owner/repo", "ghp_test_token", "https://example.com/hooks/abc", null, List.of("push"));

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(Map.class));

        HttpHeaders headers = captor.getValue().getHeaders();
        assertEquals("Bearer ghp_test_token", headers.getFirst("Authorization"));
        assertEquals("application/vnd.github+json", headers.getFirst("Accept"));
        assertEquals("2022-11-28", headers.getFirst("X-GitHub-Api-Version"));
        assertEquals("Flow-Automation-Platform", headers.getFirst("User-Agent"));
    }
}
