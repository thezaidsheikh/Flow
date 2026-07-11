package com.project.flow.execution.strategy;

import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.enums.NodeType;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Component;

@Component
public class TriggerNodeExecutionStrategy implements NodeExecutionStrategy {

    @Override
    public NodeType getSupportedType() {
        return NodeType.TRIGGER;
    }

    @Override
    public NodeExecutionResult execute(Node node, WorkflowExecutionContext context, String userId) {
        return new NodeExecutionResult(new LinkedHashMap<>(context.getTriggerData()), null);
    }
}
