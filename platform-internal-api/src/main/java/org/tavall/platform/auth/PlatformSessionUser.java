package org.tavall.platform.auth;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import org.tavall.platform.core.PlatformRole;

public record PlatformSessionUser(
        UUID platformUserId,
        UUID activeTenantAccountId,
        String email,
        String displayName,
        String avatarUrl,
        Set<PlatformRole> roles
) implements Serializable {

    public boolean hasRole(PlatformRole role) {
        return roles.contains(role);
    }
}
