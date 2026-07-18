package com.project.flow.connector.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.connector.domain.ConnectorDefinition;
import com.project.flow.connector.dto.request.ExecuteConnectorActionRequest;
import com.project.flow.connector.service.ExecuteConnectorActionService;
import com.project.flow.connector.service.ListConnectorsService;
import com.project.flow.common.security.CurrentUserProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/connectors")
@RequiredArgsConstructor
@Tag(name = "Connectors", description = "Discover available connectors and execute connector actions")
public class ConnectorController {

    private final ListConnectorsService listConnectorsService;
    private final ExecuteConnectorActionService executeConnectorActionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    @Operation(summary = "List all connectors", description = "Get all available connector integrations with their supported actions and credential schemas.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connectors retrieved successfully")
    })
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
    @Operation(summary = "Get connector detail", description = "Get a specific connector's definition, available actions, and credential schema.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connector retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Connector not found")
    })
    public ApiResponse<ConnectorDefinition> getConnector(
        @Parameter(description = "Connector provider name (e.g. github, slack)") @PathVariable String provider
    ) {
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
    @Operation(summary = "Execute connector action", description = "Execute an action on a connector using stored credentials and action-specific inputs.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Connector action executed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Connector or action not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ApiResponse<Map<String, Object>> executeConnectorAction(
        @Parameter(description = "Connector provider name") @PathVariable String provider,
        @Parameter(description = "Action to execute (e.g. create_pull_request)") @PathVariable String action,
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
