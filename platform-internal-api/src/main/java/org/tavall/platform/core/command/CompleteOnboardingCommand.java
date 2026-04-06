package org.tavall.platform.core.command;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

public record CompleteOnboardingCommand(
        UUID platformUserId,
        UUID tenantAccountId,
        @NotBlank String workspaceName,
        @NotBlank String initialSiteName,
        @NotBlank String initialSiteSlug,
        Map<String, Object> draftData
) {
}
