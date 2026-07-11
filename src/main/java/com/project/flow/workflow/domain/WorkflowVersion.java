package com.project.flow.workflow.domain;

import com.project.flow.common.domain.BaseEntity;
import com.project.flow.workflow.enums.VersionStatus;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workflow_versions", indexes = { @Index(name = "idx_workflow_versions_workflow_id", columnList = "workflow_id"), @Index(name = "idx_workflow_versions_status", columnList = "status") })
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class WorkflowVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "workflow_id", nullable = false)
    private String workflowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", insertable = false, updatable = false)
    private Workflow workflow;

    @Column(nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VersionStatus status;

    @OneToMany(mappedBy = "workflowVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Node> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "workflowVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Edge> edges = new ArrayList<>();
}
