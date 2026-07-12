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

    private static final String CREDENTIAL_ID_KEY = "credentialId";

    private final ExecuteConnectorActionService executeConnectorActionService;
    private final ExecutionTemplateResolver executionTemplateResolver;

    @Override
    public NodeType getSupportedType() {
        return NodeType.ACTION;
    }

    @Override
    public NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId) {
        Map<String, Object> config = requireMap(node.getConfig(), "Action node config must be an object");

        String provider = resolveProvider(config, node);
        String action = resolveAction(config, node);
        String credentialId = stringValue(config.get(CREDENTIAL_ID_KEY), "Action node credentialId is required");
        Map<String, Object> rawInputs = resolveInputs(config);
        Map<String, Object> resolvedInputs = executionTemplateResolver.resolveMap(rawInputs, context);
        Map<String, Object> result = executeConnectorActionService.execute(userId, credentialId, provider, action, resolvedInputs);
        return new NodeExecutionResult(result, null);
    }

    private String resolveProvider(Map<String, Object> config, Node node) {
        Object fromConfig = config.get("provider");
        if (fromConfig instanceof String s && !s.isBlank()) {
            return s;
        }
        if (node.getSubType() != null && node.getSubType().contains("_")) {
            return node.getSubType().split("_")[0];
        }
        throw new InvalidRequestException("Action node provider is required", Optional.empty());
    }

    private String resolveAction(Map<String, Object> config, Node node) {
        Object fromConfig = config.get("action");
        if (fromConfig instanceof String s && !s.isBlank()) {
            return s;
        }
        if (node.getSubType() != null) {
            return switch (node.getSubType().toLowerCase()) {
                case "github_pr" -> "create_pull_request";
                default -> node.getSubType();
            };
        }
        throw new InvalidRequestException("Action node action is required", Optional.empty());
    }

    private Map<String, Object> resolveInputs(Map<String, Object> config) {
        Object inputsObj = config.get("inputs");
        if (inputsObj instanceof Map<?, ?> inputsMap) {
            return normalizeMap(inputsMap);
        }
        Map<String, Object> inputs = new LinkedHashMap<>();
        config.forEach((key, value) -> {
            if (!"provider".equals(key) && !"action".equals(key) && !CREDENTIAL_ID_KEY.equals(key)) {
                inputs.put(key, value);
            }
        });
        return inputs;
    }

    private String stringValue(Object value, String message) {
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }
        throw new InvalidRequestException(message, Optional.empty());
    }

    private Map<String, Object> requireMap(Object value, String message) {
        if (value instanceof Map<?, ?> map) {
            return normalizeMap(map);
        }
        throw new InvalidRequestException(message, Optional.empty());
    }

    private Map<String, Object> normalizeMap(Map<?, ?> map) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        map.forEach((key, mapValue) -> normalized.put(String.valueOf(key), mapValue));
        return normalized;
    }
}
