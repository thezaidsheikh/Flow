package com.project.flow.workflow.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WorkflowResponse(
    String id,
    String name,
    String description,
    String status,
    Integer versionNumber,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
