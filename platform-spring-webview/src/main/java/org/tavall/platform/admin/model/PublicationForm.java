package org.tavall.platform.admin.model;

import java.util.UUID;

public class PublicationForm {

    private UUID siteId;
    private String versionLabel;
    private String buildReference;

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public String getBuildReference() {
        return buildReference;
    }

    public void setBuildReference(String buildReference) {
        this.buildReference = buildReference;
    }
}
