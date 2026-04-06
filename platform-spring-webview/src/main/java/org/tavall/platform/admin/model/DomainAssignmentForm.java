package org.tavall.platform.admin.model;

import java.util.UUID;

public class DomainAssignmentForm {

    private UUID siteId;
    private String host;
    private boolean primaryDomain = true;

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isPrimaryDomain() {
        return primaryDomain;
    }

    public void setPrimaryDomain(boolean primaryDomain) {
        this.primaryDomain = primaryDomain;
    }
}
