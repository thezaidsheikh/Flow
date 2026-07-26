package com.project.flow.workflow.service;

import com.project.flow.common.exception.ResourceNotFound;
import com.project.flow.run.domain.WorkflowRun;
import com.project.flow.run.repository.NodeRunLogRepository;
import com.project.flow.run.repository.WorkflowRunRepository;
import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.webhook.repository.WebhookRegistrationRepository;
import com.project.flow.workflow.webhook.service.WebhookRegistrationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteWorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final NodeRunLogRepository nodeRunLogRepository;
    private final WebhookRegistrationRepository webhookRegistrationRepository;
    private final WebhookRegistrationService webhookRegistrationService;

    @Transactional
    public void execute(String workflowId, String userId) {
        Workflow workflow = workflowRepository.findByIdAndUserId(workflowId, userId)
            .orElseThrow(() -> new ResourceNotFound("Workflow not found", null));

        webhookRegistrationService.deactivateWebhooks(workflowId);
        webhookRegistrationRepository.deleteByWorkflowId(workflowId);

        List<WorkflowRun> runs = workflowRunRepository.findByWorkflowId(workflowId);
        for (WorkflowRun run : runs) {
            nodeRunLogRepository.deleteByWorkflowRunId(run.getId());
        }
        workflowRunRepository.deleteAll(runs);

        workflowRepository.delete(workflow);
        log.info("Deleted workflow={} with GitHub webhook cleanup", workflowId);
    }
}
