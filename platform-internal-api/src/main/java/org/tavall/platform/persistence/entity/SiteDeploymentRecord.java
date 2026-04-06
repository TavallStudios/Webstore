package org.tavall.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.tavall.platform.core.AdminActionType;
import org.tavall.platform.core.SiteDeploymentStatus;

@Entity
@Table(name = "site_deployment_records", schema = "platform_sites")
@Getter
@Setter
public class SiteDeploymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_site_id", nullable = false)
    private TenantSite tenantSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_publication_id")
    private SitePublication sitePublication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orchestration_job_id")
    private OrchestrationJob orchestrationJob;

    @Column(nullable = false)
    private UUID triggeredByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private AdminActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SiteDeploymentStatus deploymentStatus = SiteDeploymentStatus.QUEUED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> runtimeSpecSnapshot = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> resultPayload = new LinkedHashMap<>();

    @Column(length = 4000)
    private String errorMessage;

    private Instant startedAt;

    private Instant completedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
