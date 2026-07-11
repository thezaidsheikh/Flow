package com.project.flow.workflow.domain;

import com.project.flow.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "edges", indexes = {
    @Index(name = "idx_edges_workflow_version_id", columnList = "workflow_version_id"),
    @Index(name = "idx_edges_source_node_id", columnList = "source_node_id"),
    @Index(name = "idx_edges_target_node_id", columnList = "target_node_id")
})
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Edge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_version_id", nullable = false)
    private String workflowVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", insertable = false, updatable = false)
    private WorkflowVersion workflowVersion;

    @Column(name = "source_node_id", nullable = false)
    private String sourceNodeId;

    @Column(name = "target_node_id", nullable = false)
    private String targetNodeId;

    @Column(length = 50)
    private String label;
}
