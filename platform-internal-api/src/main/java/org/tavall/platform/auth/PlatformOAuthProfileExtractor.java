package org.tavall.platform.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.tavall.platform.core.PlatformProviderType;

@Component
public class PlatformOAuthProfileExtractor {

    public AuthProviderProfile extract(String registrationId, Map<String, Object> attributes) {
        PlatformProviderType providerType = resolveProviderType(registrationId);
        String subject = firstNonBlank(
                stringValue(attributes.get("sub")),
                stringValue(attributes.get("id")),
                stringValue(attributes.get("oid"))
        );
        String email = firstNonBlank(
                stringValue(attributes.get("email")),
                stringValue(attributes.get("preferred_username"))
        );
        String displayName = firstNonBlank(
                stringValue(attributes.get("name")),
                stringValue(attributes.get("login")),
                stringValue(attributes.get("username")),
                email,
                providerType.name().toLowerCase()
        );
        String avatarUrl = resolveAvatarUrl(providerType, attributes);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("rawProvider", registrationId);
        metadata.put("login", stringValue(attributes.get("login")));
        metadata.put("emailVerified", attributes.get("email_verified"));
        metadata.put("username", stringValue(attributes.get("username")));
        return new AuthProviderProfile(providerType, subject, email, displayName, avatarUrl, metadata);
    }

    public PlatformProviderType resolveProviderType(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> PlatformProviderType.GOOGLE;
            case "github" -> PlatformProviderType.GITHUB;
            case "discord" -> PlatformProviderType.DISCORD;
            case "microsoft", "azure", "azuread" -> PlatformProviderType.MICROSOFT;
            default -> throw new IllegalArgumentException("Unsupported social login provider: " + registrationId);
        };
    }

    private String resolveAvatarUrl(PlatformProviderType providerType, Map<String, Object> attributes) {
        if (providerType == PlatformProviderType.DISCORD) {
            String userId = stringValue(attributes.get("id"));
            String avatarHash = stringValue(attributes.get("avatar"));
            if (userId != null && avatarHash != null) {
                return "https://cdn.discordapp.com/avatars/" + userId + "/" + avatarHash + ".png";
            }
        }
        return firstNonBlank(
                stringValue(attributes.get("picture")),
                stringValue(attributes.get("avatar_url")),
                stringValue(attributes.get("avatar"))
        );
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
