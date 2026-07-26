package com.project.flow.workflow.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GraphValidatorTest {

    private final GraphValidator validator = new GraphValidator();

    @Test
    void shouldPassForValidWorkflowWithManualTrigger() {
        Node trigger = Node.builder()
            .id("n1")
            .name("Manual Trigger")
            .type(NodeType.TRIGGER)
            .positionX(0)
            .positionY(0)
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        assertDoesNotThrow(() -> validator.validateGraph(version));
    }

    @Test
    void shouldPassForValidGitHubPushTriggerWithConfig() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(Map.of("credentialId", "cred-1", "repo", "owner/repo"))
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        assertDoesNotThrow(() -> validator.validateGraph(version));
    }

    @Test
    void shouldPassForGitHubPushTriggerWithOptionalBranchConfig() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(Map.of(
                "credentialId", "cred-1",
                "repo", "owner/repo",
                "sourceBranch", "feature/*",
                "targetBranch", "main"
            ))
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        assertDoesNotThrow(() -> validator.validateGraph(version));
    }

    @Test
    void shouldRejectGitHubPushTriggerWithoutCredentialId() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(Map.of("repo", "owner/repo"))
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
        assertTrue(ex.getMessage().contains("credentialId"));
    }

    @Test
    void shouldRejectGitHubPushTriggerWithoutRepo() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(Map.of("credentialId", "cred-1"))
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
        assertTrue(ex.getMessage().contains("repo"));
    }

    @Test
    void shouldRejectGitHubPushTriggerWithNullConfig() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(null)
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
        assertTrue(ex.getMessage().contains("configuration"));
    }

    @Test
    void shouldRejectGitHubPushTriggerWithBlankCredentialId() {
        Node trigger = Node.builder()
            .id("n1")
            .name("GitHub Push")
            .type(NodeType.TRIGGER)
            .subType("GITHUB_PUSH")
            .positionX(0)
            .positionY(0)
            .config(Map.of("credentialId", "", "repo", "owner/repo"))
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of())
            .build();

        InvalidRequestException ex = assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
        assertTrue(ex.getMessage().contains("credentialId"));
    }

    @Test
    void shouldRejectEmptyNodes() {
        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of())
            .edges(List.of())
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
    }

    @Test
    void shouldRejectWorkflowWithoutTrigger() {
        Node action = Node.builder()
            .id("n1")
            .name("Action")
            .type(NodeType.ACTION)
            .positionX(0)
            .positionY(0)
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(action))
            .edges(List.of())
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
    }

    @Test
    void shouldRejectMultipleTriggers() {
        Node trigger1 = Node.builder()
            .id("n1")
            .name("Trigger 1")
            .type(NodeType.TRIGGER)
            .positionX(0)
            .positionY(0)
            .build();
        Node trigger2 = Node.builder()
            .id("n2")
            .name("Trigger 2")
            .type(NodeType.TRIGGER)
            .positionX(100)
            .positionY(0)
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger1, trigger2))
            .edges(List.of())
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
    }

    @Test
    void shouldRejectEdgeWithNonExistentSourceNode() {
        Node trigger = Node.builder()
            .id("n1")
            .name("Trigger")
            .type(NodeType.TRIGGER)
            .positionX(0)
            .positionY(0)
            .build();
        Edge edge = Edge.builder()
            .sourceNodeId("nonexistent")
            .targetNodeId("n1")
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of(edge))
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
    }

    @Test
    void shouldRejectEdgeWithNonExistentTargetNode() {
        Node trigger = Node.builder()
            .id("n1")
            .name("Trigger")
            .type(NodeType.TRIGGER)
            .positionX(0)
            .positionY(0)
            .build();
        Edge edge = Edge.builder()
            .sourceNodeId("n1")
            .targetNodeId("nonexistent")
            .build();

        WorkflowVersion version = WorkflowVersion.builder()
            .id("v1")
            .nodes(List.of(trigger))
            .edges(List.of(edge))
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validateGraph(version));
    }
}
