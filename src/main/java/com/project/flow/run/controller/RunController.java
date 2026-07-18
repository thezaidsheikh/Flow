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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Workflow Executions", description = "Run workflows, list executions, view details and logs")
public class RunController {

    private final RunWorkflowService runWorkflowService;
    private final ListWorkflowRunsService listWorkflowRunsService;
    private final GetRunService getRunService;
    private final GetRunLogsService getRunLogsService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/workflows/{id}/run")
    @Operation(summary = "Execute workflow", description = "Trigger a published workflow with optional trigger data and variables.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow executed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Workflow is not published or validation failed")
    })
    public ApiResponse<WorkflowRunDetailResponse> runWorkflow(
        @Parameter(description = "Workflow ID (must be published)") @PathVariable String id,
        @Valid @RequestBody(required = false) RunWorkflowRequest request
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunDetailResponse response = runWorkflowService.execute(id, request, userId);
        return ApiResponse.<WorkflowRunDetailResponse>builder().success(true).statusCode(200).message("Workflow executed successfully").data(response).build();
    }

    @GetMapping("/workflows/{id}/runs")
    @Operation(summary = "List workflow runs", description = "Get paginated execution history for a workflow.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow runs retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ApiResponse<WorkflowRunPageResponse> listWorkflowRuns(
        @Parameter(description = "Workflow ID") @PathVariable String id,
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Items per page (1-100)") @RequestParam(defaultValue = "20") int size
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunPageResponse response = listWorkflowRunsService.execute(id, userId, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ApiResponse.<WorkflowRunPageResponse>builder().success(true).statusCode(200).message("Workflow runs retrieved successfully").data(response).build();
    }

    @GetMapping("/runs/{runId}")
    @Operation(summary = "Get run detail", description = "Get full details of a specific workflow run including input/output payloads.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow run retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Run not found")
    })
    public ApiResponse<WorkflowRunDetailResponse> getRun(
        @Parameter(description = "Workflow Run ID") @PathVariable String runId
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        WorkflowRunDetailResponse response = getRunService.execute(runId, userId);
        return ApiResponse.<WorkflowRunDetailResponse>builder().success(true).statusCode(200).message("Workflow run retrieved successfully").data(response).build();
    }

    @GetMapping("/runs/{runId}/logs")
    @Operation(summary = "Get run logs", description = "Get per-node execution logs for a specific workflow run.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow run logs retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Run not found")
    })
    public ApiResponse<List<NodeRunLogResponse>> getRunLogs(
        @Parameter(description = "Workflow Run ID") @PathVariable String runId
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        List<NodeRunLogResponse> response = getRunLogsService.execute(runId, userId);
        return ApiResponse.<List<NodeRunLogResponse>>builder().success(true).statusCode(200).message("Workflow run logs retrieved successfully").data(response).build();
    }
}
