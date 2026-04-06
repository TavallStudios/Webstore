package org.tavall.platform.control.service;

import org.tavall.platform.runtime.TenantSiteDeploymentRequest;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

public interface TenantSiteRuntimeProvisioner {

    TenantSiteDeploymentResult createOrUpdateRuntime(TenantSiteDeploymentRequest deploymentRequest);

    TenantSiteRuntimeStatus startRuntime(TenantSiteRuntimeSpec runtimeSpec);

    TenantSiteRuntimeStatus stopRuntime(TenantSiteRuntimeSpec runtimeSpec);

    TenantSiteRuntimeStatus restartRuntime(TenantSiteRuntimeSpec runtimeSpec);

    TenantSiteRuntimeStatus destroyRuntime(TenantSiteRuntimeSpec runtimeSpec);

    TenantSiteRuntimeStatus loadRuntimeStatus(TenantSiteRuntimeSpec runtimeSpec);

    KubeVirtClusterCompatibilityReport loadClusterCompatibility(TenantSiteRuntimeSpec runtimeSpec);
}
