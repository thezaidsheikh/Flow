package com.project.flow.workflow.validator;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class GraphValidator {

    private static final String GITHUB_PUSH_TRIGGER_SUBTYPE = "GITHUB_PUSH";

    public void validateGraph(WorkflowVersion version) {
        if (version.getNodes() == null || version.getNodes().isEmpty()) {
            throw new InvalidRequestException("Workflow must have at least one node", null);
        }

        boolean hasTrigger = version.getNodes().stream()
            .anyMatch(node -> node.getType() == NodeType.TRIGGER);

        if (!hasTrigger) {
            throw new InvalidRequestException("Workflow must have at least one trigger node", null);
        }

        long triggerCount = version.getNodes().stream().filter(node -> node.getType() == NodeType.TRIGGER).count();
        if (triggerCount > 1) {
            throw new InvalidRequestException("Workflow must contain only one trigger node for manual execution", null);
        }

        validateGitHubPushTriggerNodes(version);
        validateEdges(version);
    }

    private void validateGitHubPushTriggerNodes(WorkflowVersion version) {
        version.getNodes().stream()
            .filter(node -> node.getType() == NodeType.TRIGGER
                && GITHUB_PUSH_TRIGGER_SUBTYPE.equalsIgnoreCase(node.getSubType()))
            .forEach(node -> {
                if (node.getConfig() == null) {
                    throw new InvalidRequestException(
                        "GitHub push trigger node '" + node.getName() + "' must have a configuration", null);
                }
                requireConfigField(node, "credentialId");
                requireConfigField(node, "repo");
            });
    }

    @SuppressWarnings("unchecked")
    private void requireConfigField(Node node, String field) {
        if (node.getConfig() instanceof java.util.Map<?, ?> map) {
            Object value = map.get(field);
            if (value instanceof String s && !s.isBlank()) {
                return;
            }
        }
        throw new InvalidRequestException(
            "GitHub push trigger node '" + node.getName() + "' must have a '" + field + "' in its configuration", null);
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
