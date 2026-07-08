package com.project.flow.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EdgeDto(
    String id,
    @NotBlank(message = "Source node id is required")
    String sourceNodeId,
    @NotBlank(message = "Target node id is required")
    String targetNodeId,
    String label
) {}
