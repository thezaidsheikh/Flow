package com.project.flow.workflow.repository;

import com.project.flow.workflow.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, String> {
    List<Workflow> findByUserId(String userId);

    Optional<Workflow> findByIdAndUserId(String id, String userId);

    boolean existsByIdAndUserId(String id, String userId);
}
