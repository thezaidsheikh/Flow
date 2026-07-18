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
import com.project.flow.workflow.service.DeleteWorkflowService;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.repository.NodeRepository;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.service.PublishWorkflowService;
import com.project.flow.workflow.service.SaveDraftService;
import com.project.flow.workflow.webhook.domain.WebhookRegistration;
import com.project.flow.workflow.webhook.repository.WebhookRegistrationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflows", description = "Workflow CRUD operations, draft management, publishing, renaming, and deletion")
public class WorkflowController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CreateWorkflowService createWorkflowService;
    private final SaveDraftService saveDraftService;
    private final PublishWorkflowService publishWorkflowService;
    private final DeleteWorkflowService deleteWorkflowService;
    private final GetWorkflowService getWorkflowService;
    private final CurrentUserProvider currentUserProvider;
    private final WebhookRegistrationRepository webhookRegistrationRepository;
    private final NodeRepository nodeRepository;
    private final WorkflowRepository workflowRepository;
    private final HttpServletRequest httpServletRequest;

    @PostMapping
    @Operation(summary = "Create workflow", description = "Create a new empty workflow with an initial draft version.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ApiResponse<WorkflowResponse> createWorkflow(@Valid @RequestBody CreateWorkflowRequest request) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = createWorkflowService.execute(request, userId);
        WorkflowVersion latestVersion = getWorkflowService.getLatestVersionForSummary(workflow.getId());
        return ApiResponse.<WorkflowResponse>builder().success(true).statusCode(200).message("Workflow created successfully").data(toResponse(workflow, latestVersion)).build();
    }

    @GetMapping
    @Operation(summary = "List all workflows", description = "Get all workflows for the authenticated user, ordered by most recently updated.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflows retrieved successfully")
    })
    public ApiResponse<List<WorkflowResponse>> getWorkflows(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Items per page (1-100)") @RequestParam(defaultValue = "20") int size
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
    @Operation(summary = "Get workflow detail", description = "Get a single workflow with its full graph (nodes, edges) and webhook URL if published.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ApiResponse<WorkflowDetailResponse> getWorkflow(
        @Parameter(description = "Workflow ID") @PathVariable String id
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = getWorkflowService.getLatestVersion(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow retrieved successfully").data(toDetailResponse(workflow, version)).build();
    }

    @PutMapping("/{id}/draft")
    @Operation(summary = "Save draft", description = "Save or update the workflow graph (nodes and edges). This overwrites the existing draft version.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Draft saved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public ApiResponse<WorkflowDetailResponse> saveDraft(
        @Parameter(description = "Workflow ID") @PathVariable String id,
        @Valid @RequestBody SaveDraftRequest request
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = saveDraftService.execute(id, request, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Draft saved successfully").data(toDetailResponse(workflow, version)).build();
    }

    @PatchMapping("/{id}/name")
    @Operation(summary = "Rename workflow", description = "Update the name of an existing workflow.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow name updated"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed (name is required)")
    })
    public ApiResponse<WorkflowResponse> updateWorkflowName(
        @Parameter(description = "Workflow ID") @PathVariable String id,
        @RequestBody Map<String, String> body
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            throw new com.project.flow.common.exception.InvalidRequestException("Name is required", null);
        }
        Workflow workflow = getWorkflowService.getById(id, userId);
        workflow.setName(newName.trim());
        Workflow saved = workflowRepository.save(workflow);
        WorkflowVersion latestVersion = getWorkflowService.getLatestVersionForSummary(id);
        return ApiResponse.<WorkflowResponse>builder().success(true).statusCode(200).message("Workflow name updated").data(toResponse(saved, latestVersion)).build();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish workflow", description = "Publish the current draft. This creates a new published version that can be executed. If the workflow contains a webhook trigger node, a webhook URL is automatically registered.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow published successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Draft is empty or graph validation failed")
    })
    public ApiResponse<WorkflowDetailResponse> publishWorkflow(
        @Parameter(description = "Workflow ID") @PathVariable String id
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        Workflow workflow = getWorkflowService.getById(id, userId);
        WorkflowVersion version = publishWorkflowService.execute(id, userId);
        return ApiResponse.<WorkflowDetailResponse>builder().success(true).statusCode(200).message("Workflow published successfully").data(toDetailResponse(workflow, version)).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workflow", description = "Permanently delete a workflow and all its versions, nodes, edges, webhook registrations, runs, and run logs. This operation is irreversible.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Workflow deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ApiResponse<Void> deleteWorkflow(
        @Parameter(description = "Workflow ID") @PathVariable String id
    ) {
        String userId = currentUserProvider.getCurrentUserId();
        deleteWorkflowService.execute(id, userId);
        return ApiResponse.<Void>builder().success(true).statusCode(200).message("Workflow deleted successfully").build();
    }

    private int clampPageSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private WorkflowResponse toResponse(Workflow workflow, WorkflowVersion latestVersion) {
        String status = latestVersion != null ? latestVersion.getStatus().name() : "DRAFT";
        Integer versionNumber = latestVersion != null ? latestVersion.getVersionNumber() : 1;
        String webhookUrl = null;

        if ("PUBLISHED".equals(status) && latestVersion != null) {
            List<WebhookRegistration> registrations = webhookRegistrationRepository.findByWorkflowId(workflow.getId());
            if (!registrations.isEmpty()) {
                List<Node> nodes = nodeRepository.findByWorkflowVersionId(latestVersion.getId());
                Map<String, String> nodeWebhookUrlMap = registrations.stream()
                    .filter(r -> Boolean.TRUE.equals(r.getActive()))
                    .collect(Collectors.toMap(WebhookRegistration::getNodeId, r -> {
                        String baseUrl = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
                        if (httpServletRequest.getServerPort() != 80 && httpServletRequest.getServerPort() != 443) {
                            baseUrl += ":" + httpServletRequest.getServerPort();
                        }
                        return baseUrl + "/api/v1/hooks/" + r.getPath();
                    }));

                webhookUrl = nodes.stream()
                    .filter(n -> n.getType() == NodeType.TRIGGER
                        && ("WEBHOOK".equalsIgnoreCase(n.getSubType()) || "GITHUB_PUSH".equalsIgnoreCase(n.getSubType())))
                    .map(n -> nodeWebhookUrlMap.get(n.getId()))
                    .filter(u -> u != null)
                    .findFirst()
                    .orElse(null);
            }
        }

        return new WorkflowResponse(workflow.getId(), workflow.getName(), workflow.getDescription(), status, versionNumber, workflow.getCreatedAt(), workflow.getUpdatedAt(), webhookUrl);
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
        String workflowWebhookUrl = null;

        List<NodeResponse> nodeResponses = version
            .getNodes()
            .stream()
            .map(node -> {
                String webhookUrl = null;
                if (isPublished) {
                    webhookUrl = buildWebhookUrl(node, workflow.getId());
                }
                return new NodeResponse(node.getId(), node.getClientId(), node.getName(), node.getType().name(), node.getSubType(), node.getPositionX(), node.getPositionY(), node.getConfig(), webhookUrl);
            })
            .toList();

        if (isPublished) {
            workflowWebhookUrl = nodeResponses.stream()
                .map(NodeResponse::webhookUrl)
                .filter(u -> u != null)
                .findFirst()
                .orElse(null);
        }

        return new WorkflowDetailResponse(
            workflow.getId(),
            workflow.getName(),
            workflow.getDescription(),
            version.getStatus().name(),
            version.getVersionNumber(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            nodeResponses,
            version
                .getEdges()
                .stream()
                .map(edge -> new EdgeResponse(edge.getId(), edge.getSourceNodeId(), edge.getTargetNodeId(), edge.getLabel()))
                .toList(),
            workflowWebhookUrl
        );
    }
}
