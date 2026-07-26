package com.project.flow.run.service;

import com.project.flow.run.domain.WorkflowRun;
import com.project.flow.run.dto.response.WorkflowRunPageResponse;
import com.project.flow.run.dto.response.WorkflowRunSummaryResponse;
import com.project.flow.run.repository.WorkflowRunRepository;
import com.project.flow.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAllRunsService {

    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowRepository workflowRepository;

    @Transactional(readOnly = true)
    public WorkflowRunPageResponse execute(String userId, int page, int size) {
        var result = workflowRunRepository.findByUserIdOrderByStartedAtDesc(userId, PageRequest.of(page, size));
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
