package com.project.flow.workflow.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.common.security.CurrentUserProvider;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.CreateWorkflowRequest;
import com.project.flow.workflow.dto.request.SaveDraftRequest;
import com.project.flow.workflow.dto.response.EdgeResponse;
import com.project.flow.workflow.dto.response.NodeResponse;
import com.project.flow.workflow.dto.response.WorkflowDetailResponse;
import com.project.flow.workflow.dto.response.WorkflowResponse;
import com.project.flow.workflow.service.CreateWorkflowService;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.service.PublishWorkflowService;
import com.project.flow.workflow.service.SaveDraftService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final CreateWorkflowService createWorkflowService;
    private final SaveDraftService saveDraftService;
    private final PublishWorkflowService publishWorkflowService;
    private final GetWorkflowService getWorkflowService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ApiResponse<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = createWorkflowService.execute(request, userId);
        WorkflowVersion latestVersion = getWorkflowService.getLatestVersionForSummary(workflow.getId());
        return ApiResponse.<WorkflowResponse>builder().success(true).statusCode(200).message("Workflow created successfully").data(toResponse(workflow, latestVersion)).build();
    }

    @GetMapping
    public ApiResponse<List<WorkflowResponse>> getWorkflows() {
        String userId = currentUserProvider.getCurrentUserId();
        List<Workflow> workflows = getWorkflowService.getAllByUserId(userId);
        List<WorkflowResponse> response = workflows.stream().map(workflow -> toResponse(workflow, getWorkflowService.getLatestVersionForSummary(workflow.getId()))).toList();
        return ApiResponse.<List<WorkflowResponse>>builder().success(true).statusCode(200).message("Workflows retrieved successfully").data(response).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDetailResponse> getWorkflow(@PathVariable String id) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = getWorkflowService.getLatestVersion(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow retrieved successfully").data(toDetailResponse(workflow, version)).build();
    }

    @PutMapping("/{id}/draft")
    public ApiResponse<WorkflowDetailResponse> saveDraft(@PathVariable String id, @Valid @RequestBody SaveDraftRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = saveDraftService.execute(id, request, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Draft saved successfully").data(toDetailResponse(workflow, version)).build();
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<WorkflowDetailResponse> publishWorkflow(@PathVariable String id) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = publishWorkflowService.execute(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow published successfully").data(toDetailResponse(workflow, version)).build();
    }

    private WorkflowResponse toResponse(Workflow workflow, WorkflowVersion latestVersion) {
        String status = latestVersion != null ? latestVersion.getStatus().name() : "DRAFT";
        Integer versionNumber = latestVersion != null ? latestVersion.getVersionNumber() : 1;
        return new WorkflowResponse(workflow.getId(), workflow.getName(), workflow.getDescription(), status, versionNumber, workflow.getCreatedAt(), workflow.getUpdatedAt());
    }

    private WorkflowDetailResponse toDetailResponse(Workflow workflow, WorkflowVersion version) {
        return new WorkflowDetailResponse(
            workflow.getId(),
            workflow.getName(),
            workflow.getDescription(),
            version.getStatus().name(),
            version.getVersionNumber(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            version
                .getNodes()
                .stream()
                .map(node -> new NodeResponse(node.getId(), node.getName(), node.getType().name(), node.getSubType(), node.getPositionX(), node.getPositionY(), node.getConfig()))
                .toList(),
            version
                .getEdges()
                .stream()
                .map(edge -> new EdgeResponse(edge.getId(), edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getLabel()))
                .toList()
        );
    }
}
