package org.tavall.platform.control.service;

import io.fabric8.kubernetes.api.model.Node;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;

@Component
public class KubeVirtClusterCompatibilityEvaluator {

    public KubeVirtClusterCompatibilityReport evaluate(TenantSiteRuntimeSpec runtimeSpec, List<Node> nodes) {
        return evaluate(runtimeSpec, nodes, false);
    }

    public KubeVirtClusterCompatibilityReport evaluate(
            TenantSiteRuntimeSpec runtimeSpec,
            List<Node> nodes,
            boolean emulationEnabled
    ) {
        List<Node> schedulableNodes = nodes.stream()
                .filter(node -> node.getSpec() == null || !Boolean.TRUE.equals(node.getSpec().getUnschedulable()))
                .toList();
        if (schedulableNodes.isEmpty()) {
            return incompatible(runtimeSpec.infrastructureProfile(), 0, 0, 0, 0, emulationEnabled, runtimeSpec.nodeSelector(),
                    "No schedulable Kubernetes nodes are available for tenant runtime placement.");
        }
        List<Node> selectedNodes = schedulableNodes.stream()
                .filter(node -> matchesNodeSelector(node, runtimeSpec.nodeSelector()))
                .toList();
        if (selectedNodes.isEmpty()) {
            String message = runtimeSpec.nodeSelector().isEmpty()
                    ? "No schedulable Kubernetes nodes are available for tenant runtime placement."
                    : "No schedulable Kubernetes nodes match node selector " + formatNodeSelector(runtimeSpec.nodeSelector()) + ".";
            return incompatible(runtimeSpec.infrastructureProfile(), 0, 0, 0, 0, emulationEnabled, runtimeSpec.nodeSelector(), message);
        }

        int arm64NodeCount = (int) selectedNodes.stream().filter(node -> "arm64".equalsIgnoreCase(nodeArchitecture(node))).count();
        int x86NodeCount = (int) selectedNodes.stream().filter(node -> isX86Architecture(nodeArchitecture(node))).count();
        int kvmCapableNodeCount = (int) selectedNodes.stream()
                .filter(node -> hasAllocatableDevice(node, "devices.kubevirt.io/kvm"))
                .count();

        return switch (runtimeSpec.infrastructureProfile()) {
            case DEDICATED_KUBEVIRT -> kvmCapableNodeCount > 0
                    ? compatible(runtimeSpec.infrastructureProfile(), selectedNodes.size(), arm64NodeCount, x86NodeCount, kvmCapableNodeCount, emulationEnabled, runtimeSpec.nodeSelector(),
                            "Dedicated KubeVirt target has allocatable /dev/kvm capacity.")
                    : incompatible(runtimeSpec.infrastructureProfile(), selectedNodes.size(), arm64NodeCount, x86NodeCount, kvmCapableNodeCount, emulationEnabled, runtimeSpec.nodeSelector(),
                            "Dedicated KubeVirt profile requires at least one schedulable node with allocatable /dev/kvm.");
            case NESTED_KUBEVIRT -> evaluateNestedProfile(selectedNodes.size(), arm64NodeCount, x86NodeCount, kvmCapableNodeCount, emulationEnabled, runtimeSpec.nodeSelector());
            case AUTO -> evaluateAutoProfile(selectedNodes.size(), arm64NodeCount, x86NodeCount, kvmCapableNodeCount, emulationEnabled, runtimeSpec.nodeSelector());
        };
    }

    private KubeVirtClusterCompatibilityReport evaluateAutoProfile(
            int schedulableNodeCount,
            int arm64NodeCount,
            int x86NodeCount,
            int kvmCapableNodeCount,
            boolean emulationEnabled,
            Map<String, String> nodeSelector
    ) {
        if (kvmCapableNodeCount > 0) {
            return compatible(
                    TenantRuntimeInfrastructureProfile.AUTO,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    emulationEnabled,
                    nodeSelector,
                    "AUTO profile found schedulable KubeVirt nodes with allocatable /dev/kvm."
            );
        }
        if (x86NodeCount == schedulableNodeCount && emulationEnabled) {
            return compatible(
                    TenantRuntimeInfrastructureProfile.AUTO,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    true,
                    nodeSelector,
                    "AUTO profile is targeting x86_64 nodes without /dev/kvm, and KubeVirt emulation is enabled for software-virtualized tenant runtimes."
            );
        }
        if (arm64NodeCount == schedulableNodeCount) {
            return incompatible(
                    TenantRuntimeInfrastructureProfile.AUTO,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    emulationEnabled,
                    nodeSelector,
                    "KubeVirt software emulation on arm64 nodes without allocatable /dev/kvm is not a supported tenant runtime target. "
                            + "Use arm64 nodes with hardware virtualization enabled or schedule tenant runtimes onto x86_64/KVM-capable nodes."
            );
        }
        if (x86NodeCount == schedulableNodeCount) {
            return incompatible(
                    TenantRuntimeInfrastructureProfile.AUTO,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    false,
                    nodeSelector,
                    "AUTO profile found x86_64 nodes without allocatable /dev/kvm, but KubeVirt emulation is disabled. Enable useEmulation or provide KVM-capable nodes."
            );
        }
        return incompatible(
                TenantRuntimeInfrastructureProfile.AUTO,
                schedulableNodeCount,
                arm64NodeCount,
                x86NodeCount,
                kvmCapableNodeCount,
                emulationEnabled,
                nodeSelector,
                "AUTO profile selected a mixed-architecture node pool without allocatable /dev/kvm. Apply an explicit node selector for a compatible x86_64 emulation pool or a KVM-capable tenant node pool."
        );
    }

    private KubeVirtClusterCompatibilityReport evaluateNestedProfile(
            int schedulableNodeCount,
            int arm64NodeCount,
            int x86NodeCount,
            int kvmCapableNodeCount,
            boolean emulationEnabled,
            Map<String, String> nodeSelector
    ) {
        if (kvmCapableNodeCount > 0) {
            return compatible(
                    TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    emulationEnabled,
                    nodeSelector,
                    "Nested KubeVirt target exposes allocatable /dev/kvm to the guest node."
            );
        }
        if (x86NodeCount == schedulableNodeCount && emulationEnabled) {
            return compatible(
                    TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    true,
                    nodeSelector,
                    "Nested KubeVirt target has no allocatable /dev/kvm, but x86_64 software emulation is enabled for tenant runtimes."
            );
        }
        if (arm64NodeCount == schedulableNodeCount) {
            return incompatible(
                    TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    emulationEnabled,
                    nodeSelector,
                    "Nested KubeVirt on arm64 guest nodes requires nested virtualization to expose allocatable /dev/kvm. Arm64 software emulation is not a supported tenant runtime target."
            );
        }
        if (x86NodeCount == schedulableNodeCount) {
            return incompatible(
                    TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT,
                    schedulableNodeCount,
                    arm64NodeCount,
                    x86NodeCount,
                    kvmCapableNodeCount,
                    false,
                    nodeSelector,
                    "Nested KubeVirt profile requires nested virtualization to expose allocatable /dev/kvm or KubeVirt useEmulation enabled on an x86_64 node pool."
            );
        }
        return incompatible(
                TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT,
                schedulableNodeCount,
                arm64NodeCount,
                x86NodeCount,
                kvmCapableNodeCount,
                emulationEnabled,
                nodeSelector,
                "Nested KubeVirt profile selected a mixed-architecture node pool without allocatable /dev/kvm. Restrict scheduling to a compatible x86_64 emulation pool or a node pool with nested /dev/kvm exposure."
        );
    }

    private KubeVirtClusterCompatibilityReport compatible(
            TenantRuntimeInfrastructureProfile profile,
            int schedulableNodeCount,
            int arm64NodeCount,
            int x86NodeCount,
            int kvmCapableNodeCount,
            boolean emulationEnabled,
            Map<String, String> nodeSelector,
            String message
    ) {
        return new KubeVirtClusterCompatibilityReport(
                profile,
                true,
                schedulableNodeCount,
                arm64NodeCount,
                x86NodeCount,
                kvmCapableNodeCount,
                emulationEnabled,
                copySelector(nodeSelector),
                message
        );
    }

    private KubeVirtClusterCompatibilityReport incompatible(
            TenantRuntimeInfrastructureProfile profile,
            int schedulableNodeCount,
            int arm64NodeCount,
            int x86NodeCount,
            int kvmCapableNodeCount,
            boolean emulationEnabled,
            Map<String, String> nodeSelector,
            String message
    ) {
        return new KubeVirtClusterCompatibilityReport(
                profile,
                false,
                schedulableNodeCount,
                arm64NodeCount,
                x86NodeCount,
                kvmCapableNodeCount,
                emulationEnabled,
                copySelector(nodeSelector),
                message
        );
    }

    private String nodeArchitecture(Node node) {
        if (node.getMetadata() == null || node.getMetadata().getLabels() == null) {
            return "";
        }
        return node.getMetadata().getLabels().getOrDefault("kubernetes.io/arch", "");
    }

    private boolean isX86Architecture(String architecture) {
        return "amd64".equalsIgnoreCase(architecture) || "x86_64".equalsIgnoreCase(architecture);
    }

    private boolean matchesNodeSelector(Node node, Map<String, String> nodeSelector) {
        if (nodeSelector == null || nodeSelector.isEmpty()) {
            return true;
        }
        Map<String, String> labels = node.getMetadata() == null || node.getMetadata().getLabels() == null
                ? Map.of()
                : node.getMetadata().getLabels();
        return nodeSelector.entrySet().stream()
                .allMatch(entry -> entry.getValue().equals(labels.get(entry.getKey())));
    }

    private boolean hasAllocatableDevice(Node node, String resourceKey) {
        if (node.getStatus() == null || node.getStatus().getAllocatable() == null) {
            return false;
        }
        var quantity = node.getStatus().getAllocatable().get(resourceKey);
        if (quantity == null || quantity.getAmount() == null) {
            return false;
        }
        return new BigDecimal(quantity.getAmount()).compareTo(BigDecimal.ZERO) > 0;
    }

    private Map<String, String> copySelector(Map<String, String> nodeSelector) {
        return nodeSelector == null || nodeSelector.isEmpty() ? Map.of() : new LinkedHashMap<>(nodeSelector);
    }

    private String formatNodeSelector(Map<String, String> nodeSelector) {
        return nodeSelector.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }
}
