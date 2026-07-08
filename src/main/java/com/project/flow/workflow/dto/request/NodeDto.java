package com.project.flow.workflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NodeDto(
    String id,
    @NotBlank(message = "Node name is required")
    String name,
    @NotBlank(message = "Node type is required")
    String type,
    String subType,
    Integer positionX,
    Integer positionY,
    Object config
) {}
