package com.project.flow.workflow.controller;

import com.project.flow.common.response.ApiResponse;
import com.project.flow.common.security.CurrentUserProvider;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.CreateWorkflowRequest;
import com.project.flow.workflow.dto.request.SaveDraftRequest;
import com.project.flow.workflow.dto.response.EdgeResponse;
import com.project.flow.workflow.dto.response.NodeResponse;
import com.project.flow.workflow.dto.response.WorkflowDetailResponse;
import com.project.flow.workflow.dto.response.WorkflowResponse;
import com.project.flow.workflow.enums.NodeType;
import com.project.flow.workflow.service.CreateWorkflowService;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.service.PublishWorkflowService;
import com.project.flow.workflow.service.SaveDraftService;
import com.project.flow.workflow.webhook.repository.WebhookRegistrationRepository;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CreateWorkflowService createWorkflowService;
    private final SaveDraftService saveDraftService;
    private final PublishWorkflowService publishWorkflowService;
    private final GetWorkflowService getWorkflowService;
    private final CurrentUserProvider currentUserProvider;
    private final WebhookRegistrationRepository webhookRegistrationRepository;
    private final HttpServletRequest httpServletRequest;

    @PostMapping
    public ApiResponse<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = createWorkflowService.execute(request, userId);
        WorkflowVersion latestVersion = getWorkflowService.getLatestVersionForSummary(workflow.getId());
        return ApiResponse.<WorkflowResponse>builder().success(true).statusCode(200).message("Workflow created successfully").data(toResponse(workflow, latestVersion)).build();
    }

    @GetMapping
    public ApiResponse<List<WorkflowResponse>> getWorkflows(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), clampPageSize(size), Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Workflow> workflows = getWorkflowService.getPageByUserId(userId, pageable);
        List<WorkflowResponse> response = workflows
            .getContent()
            .stream()
            .map(workflow -> toResponse(workflow, getWorkflowService.getLatestVersionForSummary(workflow.getId())))
            .toList();
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

    private int clampPageSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private WorkflowResponse toResponse(Workflow workflow, WorkflowVersion latestVersion) {
        String status = latestVersion != null ? latestVersion.getStatus().name() : "DRAFT";
        Integer versionNumber = latestVersion != null ? latestVersion.getVersionNumber() : 1;
        return new WorkflowResponse(workflow.getId(), workflow.getName(), workflow.getDescription(), status, versionNumber, workflow.getCreatedAt(), workflow.getUpdatedAt());
    }

    private String buildWebhookUrl(Node node, String workflowId) {
        if (node.getType() != NodeType.TRIGGER) return null;
        String subType = node.getSubType();
        if (subType == null) return null;
        if (!"WEBHOOK".equalsIgnoreCase(subType) && !"GITHUB_PUSH".equalsIgnoreCase(subType)) return null;

        List<com.project.flow.workflow.webhook.domain.WebhookRegistration> registrations =
            webhookRegistrationRepository.findByWorkflowId(workflowId);

        return registrations.stream()
            .filter(r -> node.getId().equals(r.getNodeId()) && Boolean.TRUE.equals(r.getActive()))
            .findFirst()
            .map(r -> {
                String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
                if (httpServletRequest.getServerPort() != 80 && httpServletRequest.getServerPort() != 443) {
                    baseUrl += ":" + httpServletRequest.getServerPort();
                }
                return baseUrl + "/api/v1/hooks/" + r.getPath();
            })
            .orElse(null);
    }

    private WorkflowDetailResponse toDetailResponse(Workflow workflow, WorkflowVersion version) {
        boolean isPublished = "PUBLISHED".equals(version.getStatus().name());

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
                .map(node -> {
                    String webhookUrl = null;
                    if (isPublished) {
                        webhookUrl = buildWebhookUrl(node, workflow.getId());
                    }
                    return new NodeResponse(node.getId(), node.getClientId(), node.getName(), node.getType().name(), node.getSubType(), node.getPositionX(), node.getPositionY(), node.getConfig(), webhookUrl);
                })
                .toList(),
            version
                .getEdges()
                .stream()
                .map(edge -> new EdgeResponse(edge.getId(), edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getLabel()))
                .toList()
        );
    }
}
