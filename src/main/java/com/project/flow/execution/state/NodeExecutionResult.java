package com.project.flow.execution.state;

import java.util.Map;

public record NodeExecutionResult(Map<String, Object> output, String nextEdgeLabel) {}
