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
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.tavall.platform.core.TenantMembershipRole;

@Entity
@Table(name = "tenant_memberships", schema = "platform_core")
@Getter
@Setter
public class TenantMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_account_id", nullable = false)
    private TenantAccount tenantAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_user_id", nullable = false)
    private PlatformUser platformUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TenantMembershipRole role = TenantMembershipRole.OWNER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
