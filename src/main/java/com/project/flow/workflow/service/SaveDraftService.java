package com.project.flow.workflow.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.SaveDraftRequest;
import com.project.flow.workflow.enums.NodeType;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveDraftService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SaveDraftService(WorkflowRepository workflowRepository, WorkflowVersionRepository workflowVersionRepository) {
        this.workflowRepository = workflowRepository;
        this.workflowVersionRepository = workflowVersionRepository;
    }

    @Transactional
    public WorkflowVersion execute(String workflowId, SaveDraftRequest request, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion draftVersion = workflowVersionRepository.findByWorkflowIdAndStatus(workflowId, VersionStatus.DRAFT).orElseGet(() -> createNewDraftVersion(workflowId));

        draftVersion.getNodes().clear();
        draftVersion.getEdges().clear();

        List<Node> nodes = request
            .nodes()
            .stream()
            .map(nodeDto -> Node.builder()
                .workflowVersionId(draftVersion.getId())
                .name(nodeDto.name())
                .type(NodeType.valueOf(nodeDto.type().toUpperCase()))
                .subType(nodeDto.subType())
                .positionX(nodeDto.positionX())
                .positionY(nodeDto.positionY())
                .config(nodeDto.config())
                .clientId(nodeDto.id())
                .build())
            .toList();

        draftVersion.getNodes().addAll(nodes);
        entityManager.flush();

        Map<String, String> clientIdToUuid = new LinkedHashMap<>();
        for (Node node : draftVersion.getNodes()) {
            if (node.getClientId() != null) {
                clientIdToUuid.put(node.getClientId(), node.getId());
            }
        }

        List<Edge> edges = request
            .edges()
            .stream()
            .filter(edgeDto -> clientIdToUuid.containsKey(edgeDto.sourceNodeId()) && clientIdToUuid.containsKey(edgeDto.targetNodeId()))
            .map(edgeDto -> Edge.builder()
                    .workflowVersionId(draftVersion.getId())
                    .sourceNodeId(clientIdToUuid.get(edgeDto.sourceNodeId()))
                    .targetNodeId(clientIdToUuid.get(edgeDto.targetNodeId()))
                    .label(edgeDto.label())
                    .build())
            .toList();

        draftVersion.getEdges().addAll(edges);
        entityManager.flush();

        return draftVersion;
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
