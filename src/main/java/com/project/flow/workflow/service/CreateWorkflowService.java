package com.project.flow.workflow.service;

import com.project.flow.auth.repository.UserRepository;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.dto.request.CreateWorkflowRequest;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Workflow execute(CreateWorkflowRequest request, String userId) {
        userRepository.findById(userId).orElseThrow(() -> new ResourceNotFound("User not found", null));

        Workflow workflow = Workflow.builder().userId(userId).name(request.name()).description(request.description()).build();

        workflow = workflowRepository.save(workflow);

        WorkflowVersion draftVersion = WorkflowVersion.builder().workflowId(workflow.getId()).versionNumber(1).status(VersionStatus.DRAFT).build();

        draftVersion = workflowVersionRepository.save(draftVersion);

        return workflow.toBuilder().versions(java.util.List.of(draftVersion)).build();
    }
}
