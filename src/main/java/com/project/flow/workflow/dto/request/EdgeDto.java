package com.project.flow.workflow.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EdgeDto(
    String id,
    @NotBlank(message = "Source node id is required")
    String sourceNodeId,
    @NotBlank(message = "Target node id is required")
    String targetNodeId,
    String label
) {}
