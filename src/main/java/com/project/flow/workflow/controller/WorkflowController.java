package com.project.flow.workflow.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.CreateWorkflowRequest;
import com.project.flow.workflow.dto.request.SaveDraftRequest;
import com.project.flow.workflow.dto.response.WorkflowDetailResponse;
import com.project.flow.workflow.dto.response.WorkflowResponse;
import com.project.flow.workflow.service.CreateWorkflowService;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.service.PublishWorkflowService;
import com.project.flow.workflow.service.SaveDraftService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final CreateWorkflowService createWorkflowService;
    private final SaveDraftService saveDraftService;
    private final PublishWorkflowService publishWorkflowService;
    private final GetWorkflowService getWorkflowService;

    @PostMapping
    public ApiResponse<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        String userId = getCurrentUserId();
        Workflow workflow = createWorkflowService.execute(request, userId);
        return ApiResponse.<WorkflowResponse>builder().success(true).statusCode(200).message("Workflow created successfully").data(toResponse(workflow)).build();
    }

    @GetMapping
    public ApiResponse<List<WorkflowResponse>> getWorkflows() {
        String userId = getCurrentUserId();
        List<Workflow> workflows = getWorkflowService.getAllByUserId(userId);
        return ApiResponse.<List<WorkflowResponse>>builder().success(true).statusCode(200).message("Workflows retrieved successfully").data(workflows.stream().map(this::toResponse).toList()).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkflowDetailResponse> getWorkflow(@PathVariable String id) {
        String userId = getCurrentUserId();
        WorkflowVersion version = getWorkflowService.getLatestVersion(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow retrieved successfully").data(toDetailResponse(version)).build();
    }

    @PutMapping("/{id}/draft")
    public ApiResponse<WorkflowDetailResponse> saveDraft(@PathVariable String id, @Valid @RequestBody SaveDraftRequest request) {
        String userId = getCurrentUserId();
        WorkflowVersion version = saveDraftService.execute(id, request, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Draft saved successfully").data(toDetailResponse(version)).build();
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<WorkflowDetailResponse> publishWorkflow(@PathVariable String id) {
        String userId = getCurrentUserId();
        WorkflowVersion version = publishWorkflowService.execute(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow published successfully").data(toDetailResponse(version)).build();
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getPrincipal().toString();
        }
        return "user-id";
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        return new WorkflowResponse(workflow.getId(), workflow.getName(), workflow.getDescription(), "DRAFT", 1, workflow.getCreatedAt(), workflow.getUpdatedAt());
    }

    private WorkflowDetailResponse toDetailResponse(WorkflowVersion version) {
        return new WorkflowDetailResponse(
            version.getId(),
            "Workflow Name",
            "Description",
            version.getStatus().name(),
            version.getVersionNumber(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            version
                .getNodes()
                .stream()
                .map(node ->
                    new com.project.flow.workflow.dto.response.NodeResponse(
                        node.getId(),
                        node.getName(),
                        node.getType().name(),
                        node.getSubType(),
                        node.getPositionX(),
                        node.getPositionY(),
                        node.getConfig()
                    )
                )
                .toList(),
            version
                .getEdges()
                .stream()
                .map(edge -> new com.project.flow.workflow.dto.response.EdgeResponse(edge.getId(), edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getLabel()))
                .toList()
        );
    }
}
