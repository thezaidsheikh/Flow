package com.project.flow.workflow.webhook.repository;

import com.project.flow.workflow.webhook.domain.WebhookRegistration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookRegistrationRepository extends JpaRepository<WebhookRegistration, String> {

    Optional<WebhookRegistration> findByPathAndActiveTrue(String path);

    List<WebhookRegistration> findByWorkflowId(String workflowId);

    List<WebhookRegistration> findByUserId(String userId);

    @Modifying
    void deleteByWorkflowId(String workflowId);
}
