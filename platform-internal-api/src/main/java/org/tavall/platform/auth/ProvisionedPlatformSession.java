package org.tavall.platform.auth;

public record ProvisionedPlatformSession(
        PlatformSessionUser sessionUser,
        boolean onboardingRequired
) {
}
