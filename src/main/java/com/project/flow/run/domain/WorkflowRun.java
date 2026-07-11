package com.project.flow.run.domain;

import com.project.flow.common.domain.BaseEntity;
import com.project.flow.run.enums.WorkflowRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
    name = "workflow_runs",
    indexes = {
        @Index(name = "idx_workflow_runs_workflow_id", columnList = "workflow_id"),
        @Index(name = "idx_workflow_runs_user_id", columnList = "user_id"),
        @Index(name = "idx_workflow_runs_status", columnList = "status")
    }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class WorkflowRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @Column(name = "workflow_version_id", nullable = false)
    private String workflowVersionId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkflowRunStatus status;

    @Column(name = "trigger_type", nullable = false, length = 20)
    private String triggerType;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_payload", columnDefinition = "jsonb")
    private Object inputPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_payload", columnDefinition = "jsonb")
    private Object outputPayload;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;
}
