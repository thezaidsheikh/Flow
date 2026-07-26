package com.project.flow.workflow.webhook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.project.flow.credential.service.GetCredentialService;
import com.project.flow.execution.orchestrator.WorkflowExecutionOrchestrator;
import com.project.flow.run.dto.response.WorkflowRunDetailResponse;
import com.project.flow.run.enums.WorkflowRunStatus;
import com.project.flow.run.domain.WorkflowRun;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.service.GetWorkflowService;
import com.project.flow.workflow.webhook.domain.WebhookRegistration;
import com.project.flow.workflow.webhook.repository.WebhookRegistrationRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebhookRegistrationServiceTest {

    @Mock
    private WebhookRegistrationRepository webhookRegistrationRepository;
    @Mock
    private GetWorkflowService getWorkflowService;
    @Mock
    private WorkflowExecutionOrchestrator orchestrator;
    @Mock
    private GitHubWebhookService gitHubWebhookService;
    @Mock
    private GetCredentialService getCredentialService;

    private WebhookRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new WebhookRegistrationService(
            webhookRegistrationRepository,
            getWorkflowService,
            orchestrator,
            gitHubWebhookService,
            getCredentialService
        );
    }

    @Test
    void executeWebhook_shouldIgnoreWhenSourceBranchDoesNotMatch() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-1")
            .path("abc123")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch("feature")
            .targetBranch("main")
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("abc123"))
            .thenReturn(Optional.of(registration));

        byte[] payload = "{\"ref\":\"refs/heads/bugfix/hotfix\",\"repository\":{\"full_name\":\"owner/repo\"}}".getBytes();
        WorkflowRunDetailResponse response = service.executeWebhook("abc123", payload, null);

        assertNull(response);
        verifyNoInteractions(orchestrator);
    }

    @Test
    void executeWebhook_shouldTriggerWhenSourceBranchMatches() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-1")
            .path("abc123")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch("feature")
            .targetBranch("main")
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("abc123"))
            .thenReturn(Optional.of(registration));

        WorkflowVersion version = WorkflowVersion.builder()
            .id("ver-1")
            .workflowId("wf-1")
            .build();
        when(getWorkflowService.getPublishedVersionById("wf-1")).thenReturn(version);
        when(getWorkflowService.loadGraph(version)).thenReturn(version);

        WorkflowRun run = WorkflowRun.builder()
            .id("run-1")
            .workflowId("wf-1")
            .workflowVersionId("ver-1")
            .userId("user-1")
            .status(WorkflowRunStatus.COMPLETED)
            .triggerType("WEBHOOK")
            .startedAt(OffsetDateTime.now())
            .finishedAt(OffsetDateTime.now())
            .build();
        when(orchestrator.execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK")))
            .thenReturn(run);

        byte[] payload = "{\"ref\":\"refs/heads/feature\"}".getBytes();
        WorkflowRunDetailResponse response = service.executeWebhook("abc123", payload, null);

        assertNotNull(response);
        assertEquals("run-1", response.id());

        verify(orchestrator).execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK"));
    }

    @Test
    void executeWebhook_shouldTriggerWhenNoSourceBranchFilter() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-2")
            .path("def456")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch(null)
            .targetBranch(null)
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("def456"))
            .thenReturn(Optional.of(registration));

        WorkflowVersion version = WorkflowVersion.builder()
            .id("ver-1")
            .workflowId("wf-1")
            .build();
        when(getWorkflowService.getPublishedVersionById("wf-1")).thenReturn(version);
        when(getWorkflowService.loadGraph(version)).thenReturn(version);

        WorkflowRun run = WorkflowRun.builder()
            .id("run-2")
            .workflowId("wf-1")
            .workflowVersionId("ver-1")
            .userId("user-1")
            .status(WorkflowRunStatus.COMPLETED)
            .triggerType("WEBHOOK")
            .startedAt(OffsetDateTime.now())
            .finishedAt(OffsetDateTime.now())
            .build();
        when(orchestrator.execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK")))
            .thenReturn(run);

        byte[] payload = "{\"ref\":\"refs/heads/any-branch\"}".getBytes();
        WorkflowRunDetailResponse response = service.executeWebhook("def456", payload, null);

        assertNotNull(response);
        verify(orchestrator).execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK"));
    }

    @Test
    void executeWebhook_shouldSupportGlobPatternInSourceBranch() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-3")
            .path("ghi789")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch("feature/*")
            .targetBranch("main")
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("ghi789"))
            .thenReturn(Optional.of(registration));

        WorkflowVersion version = WorkflowVersion.builder()
            .id("ver-1")
            .workflowId("wf-1")
            .build();
        when(getWorkflowService.getPublishedVersionById("wf-1")).thenReturn(version);
        when(getWorkflowService.loadGraph(version)).thenReturn(version);

        WorkflowRun run = WorkflowRun.builder()
            .id("run-3")
            .workflowId("wf-1")
            .workflowVersionId("ver-1")
            .userId("user-1")
            .status(WorkflowRunStatus.COMPLETED)
            .triggerType("WEBHOOK")
            .startedAt(OffsetDateTime.now())
            .finishedAt(OffsetDateTime.now())
            .build();
        when(orchestrator.execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK")))
            .thenReturn(run);

        byte[] payload = "{\"ref\":\"refs/heads/feature/my-new-feature\"}".getBytes();
        WorkflowRunDetailResponse response = service.executeWebhook("ghi789", payload, null);

        assertNotNull(response);
        verify(orchestrator).execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK"));
    }

    @Test
    void executeWebhook_shouldEnrichTriggerDataWithBranches() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-4")
            .path("jkl012")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch("develop")
            .targetBranch("main")
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("jkl012"))
            .thenReturn(Optional.of(registration));

        WorkflowVersion version = WorkflowVersion.builder()
            .id("ver-1")
            .workflowId("wf-1")
            .build();
        when(getWorkflowService.getPublishedVersionById("wf-1")).thenReturn(version);
        when(getWorkflowService.loadGraph(version)).thenReturn(version);

        WorkflowRun run = WorkflowRun.builder()
            .id("run-4")
            .workflowId("wf-1")
            .workflowVersionId("ver-1")
            .userId("user-1")
            .status(WorkflowRunStatus.COMPLETED)
            .triggerType("WEBHOOK")
            .startedAt(OffsetDateTime.now())
            .finishedAt(OffsetDateTime.now())
            .build();
        when(orchestrator.execute(eq("wf-1"), eq(version), eq("user-1"), anyMap(), anyMap(), eq("WEBHOOK")))
            .thenReturn(run);

        byte[] payload = "{\"ref\":\"refs/heads/develop\",\"repository\":{\"full_name\":\"owner/repo\"}}".getBytes();
        service.executeWebhook("jkl012", payload, null);

        verify(orchestrator).execute(
            eq("wf-1"), eq(version), eq("user-1"),
            argThat(map -> "develop".equals(map.get("source_branch")) && "main".equals(map.get("target_branch"))),
            anyMap(), eq("WEBHOOK")
        );
    }

    @Test
    void executeWebhook_shouldIgnoreWhenSourceBranchGlobDoesNotMatch() {
        WebhookRegistration registration = WebhookRegistration.builder()
            .id("reg-5")
            .path("mno345")
            .workflowId("wf-1")
            .userId("user-1")
            .secret(null)
            .sourceBranch("release/*")
            .targetBranch("main")
            .active(true)
            .build();

        when(webhookRegistrationRepository.findByPathAndActiveTrue("mno345"))
            .thenReturn(Optional.of(registration));

        byte[] payload = "{\"ref\":\"refs/heads/feature/test\"}".getBytes();
        WorkflowRunDetailResponse response = service.executeWebhook("mno345", payload, null);

        assertNull(response);
        verifyNoInteractions(orchestrator);
    }
}
