package com.project.flow.connector.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.connector.domain.ConnectorDefinition;
import com.project.flow.connector.dto.request.ExecuteConnectorActionRequest;
import com.project.flow.connector.service.ExecuteConnectorActionService;
import com.project.flow.connector.service.ListConnectorsService;
import com.project.flow.common.security.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing REST API endpoints to read connector configurations.
 */
@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
public class ConnectorController {

    private final ListConnectorsService listConnectorsService;
    private final ExecuteConnectorActionService executeConnectorActionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ApiResponse<List<ConnectorDefinition>> listConnectors() {
        List<ConnectorDefinition> response = listConnectorsService.execute();
        return ApiResponse.<List<ConnectorDefinition>>builder()
                .success(true)
                .statusCode(200)
                .message("Connectors retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping("/{provider}")
    public ApiResponse<ConnectorDefinition> getConnector(@PathVariable String provider) {
        ConnectorDefinition connector = listConnectorsService.execute().stream()
                .filter(c -> c.provider().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new com.project.flow.common.exception.ResourceNotFound("Connector not found: " + provider, null));

        return ApiResponse.<ConnectorDefinition>builder()
                .success(true)
                .statusCode(200)
                .message("Connector retrieved successfully")
                .data(connector)
                .build();
    }

    @PostMapping("/{provider}/actions/{action}/execute")
    public ApiResponse<Map<String, Object>> executeConnectorAction(
        @PathVariable String provider,
        @PathVariable String action,
        @Valid @RequestBody ExecuteConnectorActionRequest request
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        Map<String, Object> response = executeConnectorActionService.execute(userId, request.credentialId(), provider, action, request.inputs());

        return ApiResponse.<Map<String, Object>>builder()
            .success(true)
            .statusCode(200)
            .message("Connector action executed successfully")
            .data(response)
            .build();
    }
}
