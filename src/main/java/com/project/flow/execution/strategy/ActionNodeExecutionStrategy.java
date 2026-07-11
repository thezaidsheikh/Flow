package com.project.flow.execution.strategy;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.connector.service.ExecuteConnectorActionService;
import com.project.flow.execution.state.ExecutionTemplateResolver;
import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionNodeExecutionStrategy implements NodeExecutionStrategy {

    private final ExecuteConnectorActionService executeConnectorActionService;
    private final ExecutionTemplateResolver executionTemplateResolver;

    @Override
    public NodeType getSupportedType() {
        return NodeType.ACTION;
    }

    @Override
    public NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId) {
        Map<String, Object> config = requireMap(node.getConfig(), "Action node config must be an object");
        String provider = stringValue(config.get("provider"), "Action node provider is required");
        String action = stringValue(config.get("action"), "Action node action is required");
        String credentialId = stringValue(config.get("credentialId"), "Action node credentialId is required");
        Map<String, Object> rawInputs = requireMap(config.getOrDefault("inputs", Map.of()), "Action node inputs must be an object");
        Map<String, Object> resolvedInputs = executionTemplateResolver.resolveMap(rawInputs, context);
        Map<String, Object> result = executeConnectorActionService.execute(userId, credentialId, provider, action, resolvedInputs);
        return new NodeExecutionResult(result, null);
    }

    private String stringValue(Object value, String message) {
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }

        throw new InvalidRequestException(message, Optional.empty());
    }

    private Map<String, Object> requireMap(Object value, String message) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            map.forEach((key, mapValue) -> normalized.put(String.valueOf(key), mapValue));
            return normalized;
        }

        throw new InvalidRequestException(message, Optional.empty());
    }
}
