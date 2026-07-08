package com.project.flow.workflow.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;

    public Workflow getById(String workflowId, String userId) {
        return workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));
    }

    public List<Workflow> getAllByUserId(String userId) {
        return workflowRepository.findByUserId(userId);
    }

    public WorkflowVersion getLatestVersion(String workflowId, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        return workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc(workflowId).orElseThrow(() -> new ResourceNotFound("No version found", null));
    }
}
