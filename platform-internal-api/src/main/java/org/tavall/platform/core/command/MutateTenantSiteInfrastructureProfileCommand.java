package org.tavall.platform.core.command;

import java.util.UUID;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;

public record MutateTenantSiteInfrastructureProfileCommand(
        UUID siteId,
        UUID requestedByUserId,
        TenantRuntimeInfrastructureProfile infrastructureProfile
) {
}
