package com.project.flow.workflow.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.SaveDraftRequest;
import com.project.flow.workflow.enums.NodeType;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.EdgeRepository;
import com.project.flow.workflow.repository.NodeRepository;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveDraftService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;

    @Transactional
    public WorkflowVersion execute(String workflowId, SaveDraftRequest request, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion draftVersion = workflowVersionRepository.findByWorkflowIdAndStatus(workflowId, VersionStatus.DRAFT).orElseGet(() -> createNewDraftVersion(workflowId));

        nodeRepository.deleteByWorkflowVersionId(draftVersion.getId());
        edgeRepository.deleteByWorkflowVersionId(draftVersion.getId());

        List<Node> nodes = request
            .nodes()
            .stream()
            .map(nodeDto ->
                Node.builder()
                    .id(nodeDto.id() != null ? nodeDto.id() : UUID.randomUUID().toString())
                    .workflowVersionId(draftVersion.getId())
                    .name(nodeDto.name())
                    .type(NodeType.valueOf(nodeDto.type().toUpperCase()))
                    .subType(nodeDto.subType())
                    .positionX(nodeDto.positionX())
                    .positionY(nodeDto.positionY())
                    .config(nodeDto.config())
                    .build()
            )
            .toList();

        List<Edge> edges = request
            .edges()
            .stream()
            .map(edgeDto ->
                Edge.builder()
                    .id(edgeDto.id() != null ? edgeDto.id() : UUID.randomUUID().toString())
                    .workflowVersionId(draftVersion.getId())
                    .sourceNodeId(edgeDto.sourceNodeId())
                    .targetNodeId(edgeDto.targetNodeId())
                    .label(edgeDto.label())
                    .build()
            )
            .toList();

        nodeRepository.saveAll(nodes);
        edgeRepository.saveAll(edges);

        return draftVersion.toBuilder().nodes(nodes).edges(edges).build();
    }

    private WorkflowVersion createNewDraftVersion(String workflowId) {
        Integer nextVersion = workflowVersionRepository
            .findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId)
            .map(version -> version.getVersionNumber() + 1)
            .orElse(1);

        WorkflowVersion newVersion = WorkflowVersion.builder().workflowId(workflowId).versionNumber(nextVersion).status(VersionStatus.DRAFT).build();

        return workflowVersionRepository.save(newVersion);
    }
}
