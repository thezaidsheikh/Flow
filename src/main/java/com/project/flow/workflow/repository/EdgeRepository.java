package com.project.flow.workflow.repository;

import com.project.flow.workflow.domain.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, String> {
    List<Edge> findByWorkflowVersionId(String workflowVersionId);

    @Modifying(clearAutomatically = true)
    void deleteByWorkflowVersionId(String workflowVersionId);
}
