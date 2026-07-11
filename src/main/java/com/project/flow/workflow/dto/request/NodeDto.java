package com.project.flow.workflow.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NodeDto(
    String id,
    @NotBlank(message = "Node name is required")
    String name,
    @NotBlank(message = "Node type is required")
    String type,
    String subType,
    @NotNull(message = "Position X is required")
    Integer positionX,
    @NotNull(message = "Position Y is required")
    Integer positionY,
    Object config
) {}
