package com.project.flow.run.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.run.domain.WorkflowRun;
import com.project.flow.run.dto.response.WorkflowRunPageResponse;
import com.project.flow.run.dto.response.WorkflowRunSummaryResponse;
import com.project.flow.run.repository.WorkflowRunRepository;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListWorkflowRunsService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowRunRepository workflowRunRepository;

    @Transactional(readOnly = true)
    public WorkflowRunPageResponse execute(String workflowId, String userId, int page, int size) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        var result = workflowRunRepository.findByWorkflowIdAndUserIdOrderByStartedAtDesc(workflowId, userId, PageRequest.of(page, size));
        var items = result
            .getContent()
            .stream()
            .map(this::mapToSummaryResponse)
            .toList();

        return new WorkflowRunPageResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private WorkflowRunSummaryResponse mapToSummaryResponse(WorkflowRun run) {
        String workflowName = workflowRepository
            .findByIdAndUserId(run.getWorkflowId(), run.getUserId())
            .map(w -> w.getName())
            .orElse("Unknown Workflow");
        return new WorkflowRunSummaryResponse(
            run.getId(),
            run.getWorkflowId(),
            workflowName,
            run.getWorkflowVersionId(),
            run.getStatus().name(),
            run.getTriggerType(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getErrorMessage()
        );
    }
}
