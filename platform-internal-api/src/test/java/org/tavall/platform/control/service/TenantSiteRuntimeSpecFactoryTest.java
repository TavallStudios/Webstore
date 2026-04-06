package org.tavall.platform.control.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tavall.platform.control.config.ControlPlaneProperties;
import org.tavall.platform.core.SitePublicationStatus;
import org.tavall.platform.core.TenantAccountStatus;
import org.tavall.platform.persistence.entity.SitePublication;
import org.tavall.platform.persistence.entity.SiteRuntimeDefinition;
import org.tavall.platform.persistence.entity.TenantAccount;
import org.tavall.platform.persistence.entity.TenantSite;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;

class TenantSiteRuntimeSpecFactoryTest {

    @Test
    void appliesConfiguredNestedSelectorWhenProfileUsesNestedKubeVirt() {
        ControlPlaneProperties properties = new ControlPlaneProperties();
        properties.getKubernetes().setNestedNodeSelector("platform.tavall/runtime-profile=nested-kubevirt,node.kubernetes.io/instance-type=vm-host");
        TenantSiteRuntimeSpecFactory factory = new TenantSiteRuntimeSpecFactory(properties);

        TenantSiteRuntimeSpec runtimeSpec = factory.buildTenantSiteRuntimeSpec(
                site(),
                runtimeDefinition(Map.of("infrastructureProfile", TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT.name())),
                publication(),
                "demo.example.test"
        );

        assertThat(runtimeSpec.infrastructureProfile()).isEqualTo(TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT);
        assertThat(runtimeSpec.nodeSelector())
                .containsEntry("platform.tavall/runtime-profile", "nested-kubevirt")
                .containsEntry("node.kubernetes.io/instance-type", "vm-host");
    }

    @Test
    void explicitDesiredConfigNodeSelectorOverridesProfileDefaultSelector() {
        ControlPlaneProperties properties = new ControlPlaneProperties();
        properties.getKubernetes().setDedicatedNodeSelector("platform.tavall/runtime-profile=dedicated-kubevirt");
        TenantSiteRuntimeSpecFactory factory = new TenantSiteRuntimeSpecFactory(properties);

        TenantSiteRuntimeSpec runtimeSpec = factory.buildTenantSiteRuntimeSpec(
                site(),
                runtimeDefinition(Map.of(
                        "infrastructureProfile", TenantRuntimeInfrastructureProfile.DEDICATED_KUBEVIRT.name(),
                        "nodeSelector", Map.of("platform.tavall/runtime-profile", "custom-dedicated")
                )),
                publication(),
                "demo.example.test"
        );

        assertThat(runtimeSpec.nodeSelector()).containsExactly(Map.entry("platform.tavall/runtime-profile", "custom-dedicated"));
    }

    private TenantSite site() {
        TenantAccount tenantAccount = new TenantAccount();
        tenantAccount.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        tenantAccount.setSlug("demo-tenant");
        tenantAccount.setDisplayName("Demo Tenant");
        tenantAccount.setStatus(TenantAccountStatus.ACTIVE);

        TenantSite site = new TenantSite();
        site.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        site.setTenantAccount(tenantAccount);
        site.setCreatedByUserId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        site.setSlug("demo-store");
        site.setSiteName("Demo Store");
        site.setSiteConfiguration(new LinkedHashMap<>());
        return site;
    }

    private SiteRuntimeDefinition runtimeDefinition(Map<String, Object> desiredConfig) {
        SiteRuntimeDefinition runtimeDefinition = new SiteRuntimeDefinition();
        runtimeDefinition.setDesiredCpuCores(2);
        runtimeDefinition.setDesiredMemoryMiB(2048);
        runtimeDefinition.setDesiredStorageGiB(30);
        runtimeDefinition.setRuntimeNamespace("tenant-demo-store");
        runtimeDefinition.setVirtualMachineName("demo-store-vm");
        runtimeDefinition.setServiceName("demo-store-svc");
        runtimeDefinition.setIngressName("demo-store-ing");
        runtimeDefinition.setDesiredConfig(new LinkedHashMap<>(desiredConfig));
        return runtimeDefinition;
    }

    private SitePublication publication() {
        SitePublication publication = new SitePublication();
        publication.setVersionLabel("v1");
        publication.setBuildReference("webstore-view:latest");
        publication.setPublicationStatus(SitePublicationStatus.PUBLISHED);
        return publication;
    }
}
