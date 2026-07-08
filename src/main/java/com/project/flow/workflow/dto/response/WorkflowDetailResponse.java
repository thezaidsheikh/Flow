package com.project.flow.workflow.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record WorkflowDetailResponse(
    String id,
    String name,
    String description,
    String status,
    Integer versionNumber,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<NodeResponse> nodes,
    List<EdgeResponse> edges
) {}
