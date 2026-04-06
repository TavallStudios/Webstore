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
import org.tavall.platform.core.OrchestrationJobStatus;
import org.tavall.platform.core.OrchestrationJobType;

@Entity
@Table(name = "orchestration_jobs", schema = "platform_sites")
@Getter
@Setter
public class OrchestrationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_site_id", nullable = false)
    private TenantSite tenantSite;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private OrchestrationJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrchestrationJobStatus jobStatus = OrchestrationJobStatus.QUEUED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> requestPayload = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> resultPayload = new LinkedHashMap<>();

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 4000)
    private String lastErrorMessage;

    @Column(nullable = false)
    private Instant queuedAt = Instant.now();

    private Instant startedAt;

    private Instant finishedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
