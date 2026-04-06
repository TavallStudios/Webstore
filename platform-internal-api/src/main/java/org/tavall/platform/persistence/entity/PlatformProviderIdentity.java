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
import org.tavall.platform.core.PlatformProviderType;

@Entity
@Table(name = "provider_identities", schema = "platform_auth")
@Getter
@Setter
public class PlatformProviderIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "platform_user_id", nullable = false)
    private PlatformUser platformUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PlatformProviderType providerType;

    @Column(nullable = false)
    private String providerSubject;

    @Column(length = 320)
    private String emailSnapshot;

    private String displayNameSnapshot;

    @Column(length = 1000)
    private String avatarUrlSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> providerMetadata = new LinkedHashMap<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant lastLoginAt;
}
