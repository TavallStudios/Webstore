package org.tavall.platform.core.command;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record AssignTenantSiteDomainCommand(
        UUID siteId,
        UUID requestedByUserId,
        @NotBlank String host,
        boolean primaryDomain
) {
}
