package org.tavall.platform.control.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimeResources;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;

class KubeVirtClusterCompatibilityEvaluatorTest {

    private final KubeVirtClusterCompatibilityEvaluator evaluator = new KubeVirtClusterCompatibilityEvaluator();

    @Test
    void dedicatedProfileRequiresKvmCapableNode() {
        KubeVirtClusterCompatibilityReport report = evaluator.evaluate(
                runtimeSpec(TenantRuntimeInfrastructureProfile.DEDICATED_KUBEVIRT),
                List.of(node("dedicated-a", "amd64", true)),
                false
        );

        assertThat(report.compatible()).isTrue();
        assertThat(report.kvmCapableNodeCount()).isEqualTo(1);
        assertThat(report.infrastructureProfile()).isEqualTo(TenantRuntimeInfrastructureProfile.DEDICATED_KUBEVIRT);
    }

    @Test
    void nestedProfileFailsWithoutNestedKvmExposure() {
        KubeVirtClusterCompatibilityReport report = evaluator.evaluate(
                runtimeSpec(TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT),
                List.of(node("nested-a", "arm64", false)),
                true
        );

        assertThat(report.compatible()).isFalse();
        assertThat(report.message()).contains("Arm64 software emulation is not a supported tenant runtime target");
    }

    @Test
    void autoProfileRejectsArmOnlyClusterWithoutKvm() {
        KubeVirtClusterCompatibilityReport report = evaluator.evaluate(
                runtimeSpec(TenantRuntimeInfrastructureProfile.AUTO),
                List.of(node("arm-a", "arm64", false)),
                true
        );

        assertThat(report.compatible()).isFalse();
        assertThat(report.arm64NodeCount()).isEqualTo(1);
        assertThat(report.message()).contains("software emulation on arm64 nodes without allocatable /dev/kvm");
    }

    @Test
    void nestedProfileAllowsX86EmulationWhenEnabled() {
        KubeVirtClusterCompatibilityReport report = evaluator.evaluate(
                runtimeSpec(TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT),
                List.of(node("nested-x86", "amd64", false)),
                true
        );

        assertThat(report.compatible()).isTrue();
        assertThat(report.emulationEnabled()).isTrue();
        assertThat(report.message()).contains("software emulation is enabled");
    }

    @Test
    void selectorRestrictsCompatibilityToMatchingNodePool() {
        KubeVirtClusterCompatibilityReport report = evaluator.evaluate(
                runtimeSpec(
                        TenantRuntimeInfrastructureProfile.DEDICATED_KUBEVIRT,
                        Map.of("platform.tavall/runtime-profile", "dedicated-kubevirt")
                ),
                List.of(
                        node("nested-x86", "amd64", true, Map.of("platform.tavall/runtime-profile", "nested-kubevirt")),
                        node("dedicated-x86", "amd64", true, Map.of("platform.tavall/runtime-profile", "dedicated-kubevirt"))
                ),
                false
        );

        assertThat(report.compatible()).isTrue();
        assertThat(report.schedulableNodeCount()).isEqualTo(1);
        assertThat(report.nodeSelector()).containsEntry("platform.tavall/runtime-profile", "dedicated-kubevirt");
    }

    private TenantSiteRuntimeSpec runtimeSpec(TenantRuntimeInfrastructureProfile profile) {
        return runtimeSpec(profile, Map.of());
    }

    private TenantSiteRuntimeSpec runtimeSpec(TenantRuntimeInfrastructureProfile profile, Map<String, String> nodeSelector) {
        return new TenantSiteRuntimeSpec(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "demo-store",
                "tenant-demo-store",
                "demo-store-vm",
                "demo-store-svc",
                "demo-store-ing",
                "quay.io/containerdisks/fedora:40",
                "https://example.com/webstore-view.jar",
                "sha256",
                "v1",
                "store_demo",
                "demo.example.test",
                profile,
                new TenantSiteRuntimeResources(1, 1024, 20),
                nodeSelector,
                Map.of(),
                Map.of(),
                Map.of()
        );
    }

    private Node node(String name, String architecture, boolean hasKvm) {
        return node(name, architecture, hasKvm, Map.of());
    }

    private Node node(String name, String architecture, boolean hasKvm, Map<String, String> additionalLabels) {
        return new NodeBuilder()
                .withNewMetadata()
                .withName(name)
                .addToLabels("kubernetes.io/arch", architecture)
                .addToLabels(additionalLabels)
                .endMetadata()
                .withNewSpec()
                .withUnschedulable(false)
                .endSpec()
                .withNewStatus()
                .withAllocatable(hasKvm ? Map.of("devices.kubevirt.io/kvm", new Quantity("1")) : Map.of())
                .endStatus()
                .build();
    }
}
