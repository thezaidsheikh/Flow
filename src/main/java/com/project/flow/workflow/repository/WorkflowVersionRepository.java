package com.project.flow.workflow.repository;

import com.project.flow.workflow.domain.WorkflowVersion;
import com.project.flow.workflow.enums.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, String> {
    List<WorkflowVersion> findByWorkflowIdOrderByVersionNumberDesc(String workflowId);

    Optional<WorkflowVersion> findByWorkflowIdAndStatus(String workflowId, VersionStatus status);

    Optional<WorkflowVersion> findFirstByWorkflowIdOrderByVersionNumberDesc(String workflowId);
}
