package com.project.flow.workflow.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.EdgeRepository;
import com.project.flow.workflow.repository.NodeRepository;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    @Transactional(readOnly = true)
    public Workflow getById(String workflowId, String userId) {
        return workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));
    }

    @Transactional(readOnly = true)
    public List<Workflow> getAllByUserId(String userId) {
        return workflowRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<Workflow> getPageByUserId(String userId, Pageable pageable) {
        return workflowRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public WorkflowVersion getLatestVersion(String workflowId, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion version = workflowVersionRepository
            .findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId)
            .orElseThrow(() -> new ResourceNotFound("No version found", null));
        return loadGraph(version);
    }

    @Transactional(readOnly = true)
    public WorkflowVersion getPublishedVersion(String workflowId, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion version = workflowVersionRepository
            .findByWorkflowIdAndStatus(workflowId, VersionStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFound("Published workflow version not found", null));
        return loadGraph(version);
    }

    @Transactional(readOnly = true)
    public WorkflowVersion getLatestVersionForSummary(String workflowId) {
        return workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId).orElse(null);
    }

    @Transactional(readOnly = true)
    public WorkflowVersion getPublishedVersionById(String workflowId) {
        return workflowVersionRepository
            .findByWorkflowIdAndStatus(workflowId, VersionStatus.PUBLISHED)
            .orElseThrow(() -> new ResourceNotFound("Published workflow version not found", null));
    }

    public WorkflowVersion loadGraph(WorkflowVersion version) {
        List<Node> nodes = nodeRepository.findByWorkflowVersionId(version.getId());
        List<Edge> edges = edgeRepository.findByWorkflowVersionId(version.getId());
        version.getNodes().clear();
        version.getNodes().addAll(nodes);
        version.getEdges().clear();
        version.getEdges().addAll(edges);
        return version;
    }
}
