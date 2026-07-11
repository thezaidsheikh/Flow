package com.project.flow.run.dto.request;

import java.util.Map;

public record RunWorkflowRequest(Map<String, Object> triggerData, Map<String, Object> variables) {}
