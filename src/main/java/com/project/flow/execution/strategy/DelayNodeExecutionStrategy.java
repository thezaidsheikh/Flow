package com.project.flow.execution.strategy;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DelayNodeExecutionStrategy implements NodeExecutionStrategy {

    @Override
    public NodeType getSupportedType() {
        return NodeType.DELAY;
    }

    @Override
    public NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId) {
        throw new InvalidRequestException("Delay nodes are not supported by the current synchronous runtime", Optional.empty());
    }
}
