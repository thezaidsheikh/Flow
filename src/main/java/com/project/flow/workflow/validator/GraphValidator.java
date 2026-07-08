package com.project.flow.workflow.validator;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GraphValidator {

    public void validateGraph(WorkflowVersion version) {
        if (version.getNodes() == null || version.getNodes().isEmpty()) {
            throw new InvalidRequestException("Workflow must have at least one node", null);
        }

        boolean hasTrigger = version.getNodes().stream()
            .anyMatch(node -> node.getType() == NodeType.TRIGGER);

        if (!hasTrigger) {
            throw new InvalidRequestException("Workflow must have at least one trigger node", null);
        }

        validateEdges(version);
    }

    private void validateEdges(WorkflowVersion version) {
        if (version.getEdges() == null || version.getEdges().isEmpty()) {
            return;
        }

        Set<String> nodeIds = version.getNodes().stream()
            .map(Node::getId)
            .collect(java.util.stream.Collectors.toSet());

        for (Edge edge : version.getEdges()) {
            if (!nodeIds.contains(edge.getSourceNodeId())) {
                throw new InvalidRequestException(
                    "Edge references non-existent source node: " + edge.getSourceNodeId(), null);
            }
            if (!nodeIds.contains(edge.getTargetNodeId())) {
                throw new InvalidRequestException(
                    "Edge references non-existent target node: " + edge.getTargetNodeId(), null);
            }
        }
    }
}
