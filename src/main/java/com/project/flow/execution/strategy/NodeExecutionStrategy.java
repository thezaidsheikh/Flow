package com.project.flow.execution.strategy;

import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;

public interface NodeExecutionStrategy {
    NodeType getSupportedType();

    NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId);
}
