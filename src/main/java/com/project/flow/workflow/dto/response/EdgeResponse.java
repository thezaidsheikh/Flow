package com.project.flow.workflow.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EdgeResponse(
    String id,
    String sourceNodeId,
    String targetNodeId,
    String label
) {}
