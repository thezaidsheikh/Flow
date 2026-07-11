package com.project.flow.run.service;

import com.project.flow.execution.orchestrator.WorkflowExecutionOrchestrator;
import com.project.flow.run.dto.request.RunWorkflowRequest;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.service.GetWorkflowService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RunWorkflowService {

    private final GetWorkflowService getWorkflowService;
    private final WorkflowExecutionOrchestrator workflowExecutionOrchestrator;

    @Transactional
    public WorkflowRunDetailResponse execute(String workflowId, RunWorkflowRequest request, String userId) {
        WorkflowVersion version = getWorkflowService.getPublishedVersion(workflowId, userId);
        Map<String, Object> triggerData = request != null && request.triggerData() != null ? request.triggerData() : Map.of();
        Map<String, Object> variables = request != null && request.variables() != null ? request.variables() : Map.of();
        WorkflowRunDetailResponse response;
        var run = workflowExecutionOrchestrator.execute(workflowId, version, userId, triggerData, variables);
        response = new WorkflowRunDetailResponse(
            run.getId(),
            run.getWorkflowId(),
            run.getWorkflowVersionId(),
            run.getStatus().name(),
            run.getTriggerType(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getInputPayload(),
            run.getOutputPayload(),
            run.getErrorMessage()
        );
        return response;
    }
}
