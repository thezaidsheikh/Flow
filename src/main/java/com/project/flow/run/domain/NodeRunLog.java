package com.project.flow.run.domain;

import com.project.flow.common.domain.BaseEntity;
import com.project.flow.run.enums.NodeRunStatus;
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
    name = "node_run_logs",
    indexes = {
        @Index(name = "idx_node_run_logs_workflow_run_id", columnList = "workflow_run_id"),
        @Index(name = "idx_node_run_logs_node_id", columnList = "node_id"),
        @Index(name = "idx_node_run_logs_status", columnList = "status")
    }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class NodeRunLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_run_id", nullable = false)
    private String workflowRunId;

    @Column(name = "node_id", nullable = false)
    private String nodeId;

    @Column(name = "node_name", nullable = false, length = 255)
    private String nodeName;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NodeRunStatus status;

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
