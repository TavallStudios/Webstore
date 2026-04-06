package org.tavall.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

@Entity
@Table(name = "site_runtime_definitions", schema = "platform_sites")
@Getter
@Setter
public class SiteRuntimeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_site_id", nullable = false, unique = true)
    private TenantSite tenantSite;

    private String runtimeIdentifier;

    @Column(nullable = false)
    private String runtimeNamespace;

    @Column(nullable = false)
    private String virtualMachineName;

    @Column(nullable = false)
    private String serviceName;

    private String ingressName;

    @Column(nullable = false)
    private Integer desiredCpuCores;

    @Column(nullable = false)
    private Integer desiredMemoryMiB;

    @Column(nullable = false)
    private Integer desiredStorageGiB;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> desiredConfig = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> actualConfig = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> statusPayload = new LinkedHashMap<>();

    private Instant lastSynchronizedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
