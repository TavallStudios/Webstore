package org.tavall.platform.core.service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.tavall.platform.core.TenantSiteLifecycleState;
import org.tavall.platform.core.exception.PlatformDomainException;

@Component
public class TenantSiteLifecycleTransitions {

    private final Map<TenantSiteLifecycleState, Set<TenantSiteLifecycleState>> allowedTransitions =
            new EnumMap<>(TenantSiteLifecycleState.class);

    public TenantSiteLifecycleTransitions() {
        allow(TenantSiteLifecycleState.DRAFT, TenantSiteLifecycleState.READY_TO_LAUNCH, TenantSiteLifecycleState.DESTROYED);
        allow(TenantSiteLifecycleState.READY_TO_LAUNCH, TenantSiteLifecycleState.PROVISIONING, TenantSiteLifecycleState.UPDATING, TenantSiteLifecycleState.DRAFT, TenantSiteLifecycleState.DESTROYED);
        allow(TenantSiteLifecycleState.PROVISIONING, TenantSiteLifecycleState.RUNNING, TenantSiteLifecycleState.FAILED, TenantSiteLifecycleState.STOPPED);
        allow(TenantSiteLifecycleState.RUNNING, TenantSiteLifecycleState.STOPPED, TenantSiteLifecycleState.UPDATING, TenantSiteLifecycleState.FAILED, TenantSiteLifecycleState.DESTROYING);
        allow(TenantSiteLifecycleState.STOPPED, TenantSiteLifecycleState.PROVISIONING, TenantSiteLifecycleState.UPDATING, TenantSiteLifecycleState.DESTROYING);
        allow(TenantSiteLifecycleState.FAILED, TenantSiteLifecycleState.PROVISIONING, TenantSiteLifecycleState.UPDATING, TenantSiteLifecycleState.STOPPED, TenantSiteLifecycleState.DESTROYING);
        allow(TenantSiteLifecycleState.UPDATING, TenantSiteLifecycleState.RUNNING, TenantSiteLifecycleState.FAILED, TenantSiteLifecycleState.STOPPED);
        allow(TenantSiteLifecycleState.DESTROYING, TenantSiteLifecycleState.DESTROYED, TenantSiteLifecycleState.FAILED);
        allow(TenantSiteLifecycleState.DESTROYED);
    }

    public void verifyTransition(TenantSiteLifecycleState currentState, TenantSiteLifecycleState nextState) {
        if (currentState == nextState) {
            return;
        }
        Set<TenantSiteLifecycleState> permitted = allowedTransitions.getOrDefault(currentState, Set.of());
        if (!permitted.contains(nextState)) {
            throw new PlatformDomainException(
                    "Illegal site lifecycle transition from " + currentState + " to " + nextState
            );
        }
    }

    private void allow(TenantSiteLifecycleState from, TenantSiteLifecycleState... nextStates) {
        allowedTransitions.put(from, nextStates.length == 0 ? Set.of() : EnumSet.of(nextStates[0], nextStates));
    }
}
