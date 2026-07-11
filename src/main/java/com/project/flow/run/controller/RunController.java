package com.project.flow.run.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.common.security.CurrentUserProvider;
import com.project.flow.run.dto.request.RunWorkflowRequest;
import com.project.flow.run.dto.response.NodeRunLogResponse;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.run.dto.response.WorkflowRunPageResponse;
import com.project.flow.run.service.GetRunLogsService;
import com.project.flow.run.service.GetRunService;
import com.project.flow.run.service.ListWorkflowRunsService;
import com.project.flow.run.service.RunWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RunController {

    private final RunWorkflowService runWorkflowService;
    private final ListWorkflowRunsService listWorkflowRunsService;
    private final GetRunService getRunService;
    private final GetRunLogsService getRunLogsService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/workflows/{id}/run")
    public ApiResponse<WorkflowRunDetailResponse> runWorkflow(@PathVariable String id, @Valid @RequestBody(required = false) RunWorkflowRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunDetailResponse response = runWorkflowService.execute(id, request, userId);
        return ApiResponse.<WorkflowRunDetailResponse>builder().success(true).statusCode(200).message("Workflow executed successfully").data(response).build();
    }

    @GetMapping("/workflows/{id}/runs")
    public ApiResponse<WorkflowRunPageResponse> listWorkflowRuns(
        @PathVariable String id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunPageResponse response = listWorkflowRunsService.execute(id, userId, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ApiResponse.<WorkflowRunPageResponse>builder().success(true).statusCode(200).message("Workflow runs retrieved successfully").data(response).build();
    }

    @GetMapping("/runs/{runId}")
    public ApiResponse<WorkflowRunDetailResponse> getRun(@PathVariable String runId) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunDetailResponse response = getRunService.execute(runId, userId);
        return ApiResponse.<WorkflowRunDetailResponse>builder().success(true).statusCode(200).message("Workflow run retrieved successfully").data(response).build();
    }

    @GetMapping("/runs/{runId}/logs")
    public ApiResponse<List<NodeRunLogResponse>> getRunLogs(@PathVariable String runId) {
        String userId = currentUserProvider.getCurrentUserId();
        List<NodeRunLogResponse> response = getRunLogsService.execute(runId, userId);
        return ApiResponse.<List<NodeRunLogResponse>>builder().success(true).statusCode(200).message("Workflow run logs retrieved successfully").data(response).build();
    }
}
