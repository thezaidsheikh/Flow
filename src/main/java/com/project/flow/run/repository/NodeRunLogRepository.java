package com.project.flow.run.repository;

import com.project.flow.run.domain.NodeRunLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeRunLogRepository extends JpaRepository<NodeRunLog, String> {
    List<NodeRunLog> findByWorkflowRunIdOrderByStartedAtAsc(String workflowRunId);

    @Modifying
    void deleteByWorkflowRunId(String workflowRunId);
}
