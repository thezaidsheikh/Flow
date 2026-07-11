package com.project.flow.workflow.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EdgeResponse(
    String id,
    String sourceNodeId,
    String targetNodeId,
    String label
) {}
