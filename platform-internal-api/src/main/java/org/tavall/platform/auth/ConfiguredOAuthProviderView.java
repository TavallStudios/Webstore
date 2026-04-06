package org.tavall.platform.auth;

public record ConfiguredOAuthProviderView(
        String registrationId,
        String displayName,
        String authorizationUri
) {
}
