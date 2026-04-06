package org.tavall.platform.control.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RequestedByPayload(@NotNull UUID requestedByUserId) {
}
