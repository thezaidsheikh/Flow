package com.project.flow.workflow.webhook.domain;

import com.project.flow.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "webhook_registrations",
    indexes = {
        @Index(name = "idx_webhook_registrations_path", columnList = "path"),
        @Index(name = "idx_webhook_registrations_workflow_id", columnList = "workflow_id"),
        @Index(name = "idx_webhook_registrations_user_id", columnList = "user_id")
    }
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class WebhookRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String path;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @Column(name = "workflow_version_id", nullable = false)
    private String workflowVersionId;

    @Column(name = "node_id", nullable = false)
    private String nodeId;

    @Column(name = "credential_id", length = 100)
    private String credentialId;

    @Column(length = 255)
    private String secret;

    @Column(name = "source_branch", length = 255)
    private String sourceBranch;

    @Column(name = "target_branch", length = 255)
    private String targetBranch;

    @Column(name = "github_hook_id")
    private Long githubHookId;

    @Column(name = "github_hook_url", length = 500)
    private String githubHookUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
