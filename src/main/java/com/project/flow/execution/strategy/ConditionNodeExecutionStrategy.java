package com.project.flow.execution.strategy;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ConditionNodeExecutionStrategy implements NodeExecutionStrategy {

    @Override
    public NodeType getSupportedType() {
        return NodeType.CONDITION;
    }

    @Override
    public NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId) {
        Map<String, Object> config = requireMap(node.getConfig(), "Condition node config must be an object");
        String source = Optional.ofNullable(config.get("source")).map(Object::toString).orElse("trigger");
        String field = Optional
            .ofNullable(config.get("field"))
            .map(Object::toString)
            .orElseThrow(() -> new InvalidRequestException("Condition node field is required", Optional.empty()));
        String operator = Optional.ofNullable(config.get("operator")).map(Object::toString).orElse("equals");
        Object expectedValue = config.get("value");
        Object actualValue = context.resolvePath(source + "." + field);

        boolean matched = evaluate(operator, actualValue, expectedValue);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("matched", matched);
        output.put("actualValue", actualValue);
        output.put("expectedValue", expectedValue);

        return new NodeExecutionResult(output, matched ? "true" : "false");
    }

    private boolean evaluate(String operator, Object actualValue, Object expectedValue) {
        return switch (operator.toLowerCase()) {
            case "equals" -> Objects.equals(actualValue, expectedValue);
            case "not_equals" -> !Objects.equals(actualValue, expectedValue);
            case "exists" -> actualValue != null;
            default -> throw new InvalidRequestException("Unsupported condition operator: " + operator, Optional.empty());
        };
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
