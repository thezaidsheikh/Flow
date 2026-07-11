package com.project.flow.run.dto.response;

import java.util.List;

public record WorkflowRunPageResponse(
    List<WorkflowRunSummaryResponse> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
