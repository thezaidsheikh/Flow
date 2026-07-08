package com.project.flow.connector.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.project.flow.common.exception.InvalidRequestException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class GithubConnectorAdapterTest {

    private RestTemplate restTemplate;
    private GithubConnectorAdapter adapter;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        adapter = new GithubConnectorAdapter(restTemplate);
    }

    @Test
    void shouldExecuteCreatePullRequestSuccessfully() {
        // Arrange
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("repository", "octocat/Hello-World");
        inputs.put("title", "Amazing PR");
        inputs.put("head", "feature");
        inputs.put("base", "main");
        inputs.put("body", "PR description content");

        Map<String, String> secrets = Map.of("token", "ghp_mock_token_value");

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("html_url", "https://github.com/octocat/Hello-World/pull/1347");
        mockResponseBody.put("number", 1347);
        mockResponseBody.put("id", 1234567L);
        mockResponseBody.put("state", "open");

        ResponseEntity<Map> mockResponseEntity = new ResponseEntity<>(mockResponseBody, HttpStatus.CREATED);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponseEntity);

        // Act
        Map<String, Object> result = adapter.execute("create_pull_request", inputs, secrets);

        // Assert
        assertNotNull(result);
        assertEquals("https://github.com/octocat/Hello-World/pull/1347", result.get("pr_url"));
        assertEquals(1347, result.get("pr_number"));
        assertEquals("open", result.get("state"));

        // Verify request payload
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("https://api.github.com/repos/octocat/Hello-World/pulls"),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Map.class)
        );

        HttpEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        assertNotNull(capturedEntity.getHeaders().getFirst("Authorization"));
        assertEquals("Bearer ghp_mock_token_value", capturedEntity.getHeaders().getFirst("Authorization"));
        
        Map bodyMap = (Map) capturedEntity.getBody();
        assertNotNull(bodyMap);
        assertEquals("Amazing PR", bodyMap.get("title"));
        assertEquals("feature", bodyMap.get("head"));
        assertEquals("main", bodyMap.get("base"));
        assertEquals("PR description content", bodyMap.get("body"));
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenTokenIsMissing() {
        // Arrange
        Map<String, Object> inputs = Map.of(
                "repository", "octocat/Hello-World",
                "title", "Amazing PR",
                "head", "feature",
                "base", "main"
        );
        Map<String, String> emptySecrets = new HashMap<>();

        // Act & Assert
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> adapter.execute("create_pull_request", inputs, emptySecrets)
        );
        assertTrue(ex.getMessage().contains("token is missing"));
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenInputIsMissing() {
        // Arrange
        Map<String, Object> inputs = Map.of(
                "repository", "octocat/Hello-World",
                "title", "Amazing PR",
                "head", "feature"
                // base is missing
        );
        Map<String, String> secrets = Map.of("token", "ghp_token");

        // Act & Assert
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> adapter.execute("create_pull_request", inputs, secrets)
        );
        assertTrue(ex.getMessage().contains("Base branch input is required"));
    }
}
