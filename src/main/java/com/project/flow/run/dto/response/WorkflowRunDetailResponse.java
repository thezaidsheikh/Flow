package com.project.flow.run.dto.response;

import java.time.OffsetDateTime;

public record WorkflowRunDetailResponse(
    String id,
    String workflowId,
    String workflowName,
    String workflowVersionId,
    String status,
    String triggerType,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt,
    Object inputPayload,
    Object outputPayload,
    String errorMessage
) {}
