package org.tavall.platform.core;

public enum TenantSiteLifecycleState {
    DRAFT,
    READY_TO_LAUNCH,
    PROVISIONING,
    RUNNING,
    STOPPED,
    FAILED,
    UPDATING,
    DESTROYING,
    DESTROYED
}
