package com.project.flow.execution.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConditionNodeExecutionStrategyTest {

    private final ConditionNodeExecutionStrategy strategy = new ConditionNodeExecutionStrategy();

    @Test
    void shouldRouteToTrueEdgeWhenConditionMatches() {
        Node node = Node.builder()
            .name("Check Repository")
            .type(NodeType.CONDITION)
            .positionX(0)
            .positionY(0)
            .config(Map.of("source", "trigger", "field", "repository", "operator", "equals", "value", "octocat/Hello-World"))
            .build();

        WorkflowExecutionContext context = new WorkflowExecutionContext(Map.of("repository", "octocat/Hello-World"), Map.of());
        var result = strategy.execute(node, context, "user-id");

        assertEquals("true", result.nextEdgeLabel());
        assertEquals(true, result.output().get("matched"));
    }

    @Test
    void shouldRejectUnsupportedOperators() {
        Node node = Node.builder()
            .name("Unsupported Condition")
            .type(NodeType.CONDITION)
            .positionX(0)
            .positionY(0)
            .config(Map.of("source", "trigger", "field", "repository", "operator", "contains", "value", "octocat"))
            .build();

        WorkflowExecutionContext context = new WorkflowExecutionContext(Map.of("repository", "octocat/Hello-World"), Map.of());

        assertThrows(InvalidRequestException.class, () -> strategy.execute(node, context, "user-id"));
    }
}
