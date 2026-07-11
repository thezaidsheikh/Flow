package com.project.flow.run.dto.response;

import java.time.OffsetDateTime;

public record NodeRunLogResponse(
    String id,
    String nodeId,
    String nodeName,
    String nodeType,
    String status,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt,
    Object inputPayload,
    Object outputPayload,
    String errorMessage
) {}
