package org.tavall.platform.web.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class PlatformOnboardingForm {

    @NotBlank
    private String workspaceName;

    @NotBlank
    private String siteName;

    @NotBlank
    private String siteSlug;

    private String requestedDomain;

    @Min(1)
    @Max(32)
    private int cpuCores = 2;

    @Min(512)
    @Max(262144)
    private int memoryMiB = 2048;

    @Min(10)
    @Max(2048)
    private int storageGiB = 30;

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteSlug() {
        return siteSlug;
    }

    public void setSiteSlug(String siteSlug) {
        this.siteSlug = siteSlug;
    }

    public String getRequestedDomain() {
        return requestedDomain;
    }

    public void setRequestedDomain(String requestedDomain) {
        this.requestedDomain = requestedDomain;
    }

    public int getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(int cpuCores) {
        this.cpuCores = cpuCores;
    }

    public int getMemoryMiB() {
        return memoryMiB;
    }

    public void setMemoryMiB(int memoryMiB) {
        this.memoryMiB = memoryMiB;
    }

    public int getStorageGiB() {
        return storageGiB;
    }

    public void setStorageGiB(int storageGiB) {
        this.storageGiB = storageGiB;
    }
}
