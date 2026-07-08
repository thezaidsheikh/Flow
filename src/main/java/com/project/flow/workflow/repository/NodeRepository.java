package com.project.flow.workflow.repository;

import com.project.flow.workflow.domain.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeRepository extends JpaRepository<Node, String> {
    List<Node> findByWorkflowVersionId(String workflowVersionId);

    void deleteByWorkflowVersionId(String workflowVersionId);
}
