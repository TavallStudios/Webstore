package org.tavall.platform.auth;

import java.util.Map;
import org.tavall.platform.core.PlatformProviderType;

public record AuthProviderProfile(
        PlatformProviderType providerType,
        String providerSubject,
        String email,
        String displayName,
        String avatarUrl,
        Map<String, Object> providerMetadata
) {
}
