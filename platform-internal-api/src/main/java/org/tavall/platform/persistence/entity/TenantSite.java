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
import org.tavall.platform.core.TenantReadinessState;
import org.tavall.platform.core.TenantSiteLifecycleState;

@Entity
@Table(name = "tenant_sites", schema = "platform_sites")
@Getter
@Setter
public class TenantSite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_account_id", nullable = false)
    private TenantAccount tenantAccount;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String slug;

    @Column(nullable = false)
    private String siteName;

    @Column(length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TenantSiteLifecycleState lifecycleState = TenantSiteLifecycleState.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TenantReadinessState readinessState = TenantReadinessState.SITE_DRAFT_PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> siteConfiguration = new LinkedHashMap<>();

    private UUID currentPublicationId;

    @Column(length = 4000)
    private String lastErrorMessage;

    private Instant launchedAt;

    private Instant destroyedAt;

    private Instant lastStatusAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
