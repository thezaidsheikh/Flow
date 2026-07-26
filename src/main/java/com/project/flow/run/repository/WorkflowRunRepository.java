package com.project.flow.run.repository;

import com.project.flow.run.domain.WorkflowRun;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, String> {
    Page<WorkflowRun> findByWorkflowIdAndUserIdOrderByStartedAtDesc(String workflowId, String userId, Pageable pageable);

    Page<WorkflowRun> findByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);

    Optional<WorkflowRun> findByIdAndUserId(String id, String userId);

    List<WorkflowRun> findByWorkflowId(String workflowId);
}
