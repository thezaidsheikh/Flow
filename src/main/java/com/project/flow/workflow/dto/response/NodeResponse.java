package com.project.flow.workflow.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NodeResponse(
    String id,
    String name,
    String type,
    String subType,
    Integer positionX,
    Integer positionY,
    Object config
) {}
