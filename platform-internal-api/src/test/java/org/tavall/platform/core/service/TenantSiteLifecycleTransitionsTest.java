package org.tavall.platform.core.service;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.tavall.platform.core.TenantSiteLifecycleState;

class TenantSiteLifecycleTransitionsTest {

    private final TenantSiteLifecycleTransitions transitions = new TenantSiteLifecycleTransitions();

    @Test
    void allowsMutationsFromReadyToLaunchState() {
        assertThatCode(() -> transitions.verifyTransition(
                TenantSiteLifecycleState.READY_TO_LAUNCH,
                TenantSiteLifecycleState.UPDATING
        )).doesNotThrowAnyException();
    }

    @Test
    void allowsMutationsFromFailedState() {
        assertThatCode(() -> transitions.verifyTransition(
                TenantSiteLifecycleState.FAILED,
                TenantSiteLifecycleState.UPDATING
        )).doesNotThrowAnyException();
    }
}
