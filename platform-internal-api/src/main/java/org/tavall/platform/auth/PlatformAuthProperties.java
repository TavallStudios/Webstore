package org.tavall.platform.auth;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.auth")
@Getter
@Setter
public class PlatformAuthProperties {

    private Set<String> masterAdminEmails = new LinkedHashSet<>();
}
