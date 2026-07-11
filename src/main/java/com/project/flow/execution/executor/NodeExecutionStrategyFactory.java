package com.project.flow.execution.executor;

import com.project.flow.execution.strategy.NodeExecutionStrategy;
import com.project.flow.workflow.enums.NodeType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NodeExecutionStrategyFactory {

    private final Map<NodeType, NodeExecutionStrategy> strategies = new EnumMap<>(NodeType.class);

    public NodeExecutionStrategyFactory(List<NodeExecutionStrategy> strategies) {
        strategies.forEach(strategy -> this.strategies.put(strategy.getSupportedType(), strategy));
    }

    public NodeExecutionStrategy get(NodeType nodeType) {
        NodeExecutionStrategy strategy = strategies.get(nodeType);
        if (strategy == null) {
            throw new IllegalArgumentException("No execution strategy registered for node type: " + nodeType);
        }
        return strategy;
    }
}
