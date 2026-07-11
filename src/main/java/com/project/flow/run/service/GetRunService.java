package com.project.flow.run.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.run.repository.WorkflowRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRunService {

    private final WorkflowRunRepository workflowRunRepository;

    @Transactional(readOnly = true)
    public WorkflowRunDetailResponse execute(String runId, String userId) {
        var run = workflowRunRepository.findByIdAndUserId(runId, userId).orElseThrow(() -> new ResourceNotFound("Workflow run not found", null));

        return new WorkflowRunDetailResponse(
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
    }
}
