package org.tavall.platform.runtime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TenantSiteRuntimeResources(
        @Min(1) @Max(32) int cpuCores,
        @Min(512) @Max(262144) int memoryMiB,
        @Min(10) @Max(2048) int storageGiB
) {
}
