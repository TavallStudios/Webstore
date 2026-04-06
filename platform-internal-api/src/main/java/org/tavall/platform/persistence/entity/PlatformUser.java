package org.tavall.platform.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.tavall.platform.core.PlatformRole;
import org.tavall.platform.core.PlatformUserStatus;

@Entity
@Table(name = "platform_users", schema = "platform_auth")
@Getter
@Setter
public class PlatformUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 320)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(length = 1000)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PlatformUserStatus status = PlatformUserStatus.ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "platform_user_roles",
            schema = "platform_auth",
            joinColumns = @JoinColumn(name = "platform_user_id")
    )
    @Column(name = "role", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private Set<PlatformRole> roles = new LinkedHashSet<>(Set.of(PlatformRole.TENANT_USER));

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant lastLoginAt;

    public boolean hasRole(PlatformRole role) {
        return roles.contains(role);
    }
}
