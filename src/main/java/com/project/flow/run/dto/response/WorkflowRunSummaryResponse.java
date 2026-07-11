package com.project.flow.run.dto.response;

import java.time.OffsetDateTime;

public record WorkflowRunSummaryResponse(
    String id,
    String workflowId,
    String workflowVersionId,
    String status,
    String triggerType,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt,
    String errorMessage
) {}
