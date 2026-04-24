package org.tavall.platform.core.command;

import jakarta.validation.Valid;
import java.util.UUID;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;

public record MutateTenantSiteRuntimeResourcesCommand(
        UUID siteId,
        UUID requestedByUserId,
        @Valid TenantSiteRuntimeResources resources
) {
}
