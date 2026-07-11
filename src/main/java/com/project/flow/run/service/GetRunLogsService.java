package com.project.flow.run.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.run.dto.response.NodeRunLogResponse;
import com.project.flow.run.repository.NodeRunLogRepository;
import com.project.flow.run.repository.WorkflowRunRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRunLogsService {

    private final WorkflowRunRepository workflowRunRepository;
    private final NodeRunLogRepository nodeRunLogRepository;

    @Transactional(readOnly = true)
    public List<NodeRunLogResponse> execute(String runId, String userId) {
        workflowRunRepository.findByIdAndUserId(runId, userId).orElseThrow(() -> new ResourceNotFound("Workflow run not found", null));

        return nodeRunLogRepository
            .findByWorkflowRunIdOrderByStartedAtAsc(runId)
            .stream()
            .map(log -> new NodeRunLogResponse(
                log.getId(),
                log.getNodeId(),
                log.getNodeName(),
                log.getNodeType(),
                log.getStatus().name(),
                log.getStartedAt(),
                log.getFinishedAt(),
                log.getInputPayload(),
                log.getOutputPayload(),
                log.getErrorMessage()
            ))
            .toList();
    }
}
