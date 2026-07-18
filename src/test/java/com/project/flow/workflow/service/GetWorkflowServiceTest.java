package com.project.flow.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.project.flow.workflow.domain.Workflow;
import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.repository.EdgeRepository;
import com.project.flow.workflow.repository.NodeRepository;
import com.project.flow.workflow.repository.WorkflowRepository;
import com.project.flow.workflow.repository.WorkflowVersionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GetWorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowVersionRepository workflowVersionRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @InjectMocks
    private GetWorkflowService getWorkflowService;

    @Test
    void shouldReturnPagedWorkflowsForUser() {
        Workflow workflow = mock(Workflow.class);
        Page<Workflow> page = new PageImpl<>(List.of(workflow));
        when(workflowRepository.findByUserId(eq("user-1"), any(Pageable.class))).thenReturn(page);

        Page<Workflow> result = getWorkflowService.getPageByUserId("user-1", PageRequest.of(0, 20));

        assertThat(result.getContent()).containsExactly(workflow);
    }

    @Test
    void shouldNotLoadGraphWhenFetchingSummaryVersion() {
        WorkflowVersion version = mock(WorkflowVersion.class);
        when(workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc("wf-1")).thenReturn(Optional.of(version));

        WorkflowVersion result = getWorkflowService.getLatestVersionForSummary("wf-1");

        assertThat(result).isSameAs(version);
        verifyNoInteractions(nodeRepository, edgeRepository);
    }

    @Test
    void shouldReturnNullSummaryWhenNoVersionExists() {
        when(workflowVersionRepository.findFirstByWorkflowIdOrderByVersionNumberDesc("wf-1")).thenReturn(Optional.empty());

        WorkflowVersion result = getWorkflowService.getLatestVersionForSummary("wf-1");

        assertThat(result).isNull();
        verifyNoInteractions(nodeRepository, edgeRepository);
    }
}
