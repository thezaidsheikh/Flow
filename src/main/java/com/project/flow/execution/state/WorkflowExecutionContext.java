package com.project.flow.execution.state;

import java.util.LinkedHashMap;
import java.util.Map;

public class WorkflowExecutionContext {

    private final Map<String, Object> triggerData;
    private final Map<String, Object> variables;
    private Map<String, Object> previousNodeOutput;

    public WorkflowExecutionContext(Map<String, Object> triggerData, Map<String, Object> variables) {
        this.triggerData = copyMap(triggerData);
        this.variables = copyMap(variables);
        this.previousNodeOutput = new LinkedHashMap<>();
    }

    public Map<String, Object> getTriggerData() {
        return new LinkedHashMap<>(triggerData);
    }

    public Map<String, Object> getVariables() {
        return new LinkedHashMap<>(variables);
    }

    public Map<String, Object> getPreviousNodeOutput() {
        return new LinkedHashMap<>(previousNodeOutput);
    }

    public void setPreviousNodeOutput(Map<String, Object> previousNodeOutput) {
        this.previousNodeOutput = copyMap(previousNodeOutput);
    }

    public Object resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        String[] parts = path.split("\\.");
        if (parts.length == 0) {
            return null;
        }

        Object current = switch (parts[0]) {
            case "trigger" -> triggerData;
            case "variables" -> variables;
            case "previous" -> previousNodeOutput;
            default -> null;
        };

        for (int index = 1; index < parts.length && current != null; index++) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(parts[index]);
        }

        return current;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("triggerData", getTriggerData());
        snapshot.put("variables", getVariables());
        snapshot.put("previousNodeOutput", getPreviousNodeOutput());
        return snapshot;
    }

    private Map<String, Object> copyMap(Map<String, Object> source) {
        if (source == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(source);
    }
}
