package com.project.flow.workflow.service;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;

    @Transactional
    public WorkflowVersion execute(String workflowId, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion draftVersion = workflowVersionRepository
            .findByWorkflowIdAndStatus(workflowId, VersionStatus.DRAFT)
            .orElseThrow(() -> new InvalidRequestException("No draft version found to publish", null));

        validateGraph(draftVersion);

        WorkflowVersion publishedVersion = workflowVersionRepository.findByWorkflowIdAndStatus(workflowId, VersionStatus.PUBLISHED).orElse(null);

        if (publishedVersion != null) {
            publishedVersion = publishedVersion.toBuilder().status(VersionStatus.ARCHIVED).build();
            workflowVersionRepository.save(publishedVersion);
        }

        draftVersion = draftVersion.toBuilder().status(VersionStatus.PUBLISHED).build();
        return workflowVersionRepository.save(draftVersion);
    }

    private void validateGraph(WorkflowVersion version) {
        if (version.getNodes() == null || version.getNodes().isEmpty()) {
            throw new InvalidRequestException("Workflow must have at least one node", null);
        }

        boolean hasTrigger = version
            .getNodes()
            .stream()
            .anyMatch(node -> node.getType().name().equals("TRIGGER"));

        if (!hasTrigger) {
            throw new InvalidRequestException("Workflow must have at least one trigger node", null);
        }
    }
}
