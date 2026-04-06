package org.tavall.platform.admin.model;

import java.util.UUID;

public class RuntimeResourceForm {

    private UUID siteId;
    private int cpuCores;
    private int memoryMiB;
    private int storageGiB;

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
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
