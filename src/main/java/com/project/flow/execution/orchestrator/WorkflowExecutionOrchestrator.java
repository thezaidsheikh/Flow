package com.project.flow.execution.orchestrator;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.execution.executor.NodeExecutionStrategyFactory;
import com.project.flow.execution.state.NodeExecutionResult;
import com.project.flow.execution.state.WorkflowExecutionContext;
import com.project.flow.run.domain.NodeRunLog;
import com.project.flow.run.domain.WorkflowRun;
import com.project.flow.run.enums.NodeRunStatus;
import com.project.flow.run.enums.WorkflowRunStatus;
import com.project.flow.run.repository.NodeRunLogRepository;
import com.project.flow.run.repository.WorkflowRunRepository;
import com.project.flow.workflow.domain.Edge;
import com.project.flow.workflow.domain.Node;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.NodeType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class WorkflowExecutionOrchestrator {

    private static final int MAX_STEP_COUNT = 100;

    private final WorkflowRunRepository workflowRunRepository;
    private final NodeRunLogRepository nodeRunLogRepository;
    private final NodeExecutionStrategyFactory nodeExecutionStrategyFactory;

    @Transactional
    public WorkflowRun execute(String workflowId, WorkflowVersion version, String userId, Map<String, Object> triggerData, Map<String, Object> variables) {
        WorkflowExecutionContext context = new WorkflowExecutionContext(triggerData, variables);
        WorkflowRun run = workflowRunRepository.save(
            WorkflowRun.builder()
                .workflowId(workflowId)
                .workflowVersionId(version.getId())
                .userId(userId)
                .status(WorkflowRunStatus.RUNNING)
                .triggerType("MANUAL")
                .startedAt(OffsetDateTime.now())
                .inputPayload(context.snapshot())
                .build()
        );

        try {
            Node currentNode = resolveStartNode(version);
            Map<String, List<Edge>> outgoingEdges = buildOutgoingEdges(version.getEdges());
            int stepCount = 0;

            while (currentNode != null) {
                if (stepCount++ >= MAX_STEP_COUNT) {
                    throw new InvalidRequestException("Workflow exceeded the maximum supported step count", Optional.empty());
                }

                NodeRunLog log = startLog(run.getId(), currentNode, context.snapshot());
                NodeExecutionResult result = nodeExecutionStrategyFactory.get(currentNode.getType()).execute(currentNode, context, userId);
                Map<String, Object> output = copyMap(result.output());
                context.setPreviousNodeOutput(output);

                log.setStatus(NodeRunStatus.COMPLETED);
                log.setFinishedAt(OffsetDateTime.now());
                log.setOutputPayload(output);
                nodeRunLogRepository.save(log);

                currentNode = resolveNextNode(currentNode, result.nextEdgeLabel(), outgoingEdges, version.getNodes());
            }

            run.setStatus(WorkflowRunStatus.COMPLETED);
            run.setFinishedAt(OffsetDateTime.now());
            run.setOutputPayload(context.snapshot());
            return workflowRunRepository.save(run);
        } catch (RuntimeException exception) {
            run.setStatus(WorkflowRunStatus.FAILED);
            run.setFinishedAt(OffsetDateTime.now());
            run.setErrorMessage(exception.getMessage());
            workflowRunRepository.save(run);
            throw exception;
        }
    }

    private Node resolveStartNode(WorkflowVersion version) {
        return version
            .getNodes()
            .stream()
            .filter(node -> node.getType() == NodeType.TRIGGER)
            .findFirst()
            .orElseThrow(() -> new InvalidRequestException("Workflow must contain a trigger node", Optional.empty()));
    }

    private NodeRunLog startLog(String workflowRunId, Node node, Object inputPayload) {
        return nodeRunLogRepository.save(
            NodeRunLog.builder()
                .workflowRunId(workflowRunId)
                .nodeId(node.getId())
                .nodeName(node.getName())
                .nodeType(node.getType().name())
                .status(NodeRunStatus.RUNNING)
                .startedAt(OffsetDateTime.now())
                .inputPayload(inputPayload)
                .build()
        );
    }

    private Node resolveNextNode(Node currentNode, String nextEdgeLabel, Map<String, List<Edge>> outgoingEdges, List<Node> nodes) {
        List<Edge> edges = outgoingEdges.getOrDefault(currentNode.getId(), List.of());
        if (edges.isEmpty()) {
            return null;
        }

        Edge nextEdge = selectNextEdge(currentNode, edges, nextEdgeLabel);
        return nodes
            .stream()
            .filter(node -> node.getId().equals(nextEdge.getTargetNodeId()))
            .findFirst()
            .orElseThrow(() -> new InvalidRequestException("Workflow references a missing target node: " + nextEdge.getTargetNodeId(), Optional.empty()));
    }

    private Edge selectNextEdge(Node currentNode, List<Edge> edges, String nextEdgeLabel) {
        if (currentNode.getType() == NodeType.CONDITION) {
            return edges
                .stream()
                .filter(edge -> edge.getLabel() != null && edge.getLabel().equalsIgnoreCase(nextEdgeLabel))
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("Condition node is missing an outgoing edge for label: " + nextEdgeLabel, Optional.empty()));
        }

        if (edges.size() > 1) {
            throw new InvalidRequestException("Only condition nodes may branch to multiple outgoing edges", Optional.empty());
        }

        return edges.get(0);
    }

    private Map<String, List<Edge>> buildOutgoingEdges(List<Edge> edges) {
        Map<String, List<Edge>> outgoingEdges = new LinkedHashMap<>();
        for (Edge edge : edges) {
            outgoingEdges.computeIfAbsent(edge.getSourceNodeId(), ignored -> new ArrayList<>()).add(edge);
        }
        return outgoingEdges;
    }

    private Map<String, Object> copyMap(Map<String, Object> source) {
        if (source == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(source);
    }
}
