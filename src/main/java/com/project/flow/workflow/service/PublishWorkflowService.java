package com.project.flow.workflow.service;

import com.project.flow.common.exception.InvalidRequestException;
import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.VersionStatus;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import com.project.flow.workflow.validator.GraphValidator;
import com.project.flow.workflow.webhook.service.WebhookRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowVersionRepository workflowVersionRepository;
    private final GraphValidator graphValidator;
    private final GetWorkflowService getWorkflowService;
    private final WebhookRegistrationService webhookRegistrationService;

    @Transactional
    public WorkflowVersion execute(String workflowId, String userId) {
        workflowRepository.findByIdAndUserId(workflowId, userId).orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        WorkflowVersion draftVersion = getWorkflowService.loadGraph(
            workflowVersionRepository.findByWorkflowIdAndStatus(workflowId, VersionStatus.DRAFT).orElseThrow(() -> new InvalidRequestException("No draft version found to publish", null))
        );

        graphValidator.validateGraph(draftVersion);

        WorkflowVersion publishedVersion = workflowVersionRepository.findByWorkflowIdAndStatus(workflowId, VersionStatus.PUBLISHED).orElse(null);

        if (publishedVersion != null) {
            publishedVersion.setStatus(VersionStatus.ARCHIVED);
            workflowVersionRepository.save(publishedVersion);
        }

        draftVersion.setStatus(VersionStatus.PUBLISHED);
        WorkflowVersion saved = workflowVersionRepository.save(draftVersion);

        webhookRegistrationService.registerWebhooks(workflowId, userId, saved);

        return saved;
    }
}
