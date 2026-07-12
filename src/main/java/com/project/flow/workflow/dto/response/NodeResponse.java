package com.project.flow.workflow.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record NodeResponse(
    String id,
    String clientId,
    String name,
    String type,
    String subType,
    Integer positionX,
    Integer positionY,
    Object config
) {}
