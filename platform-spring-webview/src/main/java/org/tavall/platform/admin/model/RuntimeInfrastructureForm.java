package org.tavall.platform.admin.model;

import java.util.UUID;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;

public class RuntimeInfrastructureForm {

    private UUID siteId;
    private TenantRuntimeInfrastructureProfile infrastructureProfile = TenantRuntimeInfrastructureProfile.AUTO;

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public TenantRuntimeInfrastructureProfile getInfrastructureProfile() {
        return infrastructureProfile;
    }

    public void setInfrastructureProfile(TenantRuntimeInfrastructureProfile infrastructureProfile) {
        this.infrastructureProfile = infrastructureProfile;
    }
}
