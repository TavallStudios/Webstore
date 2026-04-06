package org.tavall.platform.control.service;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPathBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressServiceBackendBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tavall.platform.control.config.ControlPlaneProperties;
import org.tavall.platform.runtime.KubeVirtClusterCompatibilityReport;
import org.tavall.platform.runtime.TenantSiteDeploymentRequest;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;
import org.tavall.platform.runtime.TenantSiteRuntimePowerState;
import org.tavall.platform.runtime.TenantSiteRuntimeSpec;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@Service
public class KubeVirtTenantRuntimeProvisioner implements TenantSiteRuntimeProvisioner {

    private static final ResourceDefinitionContext VIRTUAL_MACHINE_CONTEXT = new ResourceDefinitionContext.Builder()
            .withGroup("kubevirt.io")
            .withVersion("v1")
            .withPlural("virtualmachines")
            .withNamespaced(true)
            .build();
    private static final ResourceDefinitionContext KUBEVIRT_CONTEXT = new ResourceDefinitionContext.Builder()
            .withGroup("kubevirt.io")
            .withVersion("v1")
            .withPlural("kubevirts")
            .withNamespaced(true)
            .build();

    private final ControlPlaneProperties controlPlaneProperties;
    private final KubeVirtClusterCompatibilityEvaluator compatibilityEvaluator;

    public KubeVirtTenantRuntimeProvisioner(
            ControlPlaneProperties controlPlaneProperties,
            KubeVirtClusterCompatibilityEvaluator compatibilityEvaluator
    ) {
        this.controlPlaneProperties = controlPlaneProperties;
        this.compatibilityEvaluator = compatibilityEvaluator;
    }

    @Override
    public TenantSiteDeploymentResult createOrUpdateRuntime(TenantSiteDeploymentRequest deploymentRequest) {
        TenantSiteRuntimeSpec runtimeSpec = deploymentRequest.runtimeSpec();
        if (controlPlaneProperties.getKubernetes().isDryRun()) {
            return dryRunResult(runtimeSpec, "Dry-run runtime manifest generated.");
        }
        try (KubernetesClient client = createClient()) {
            KubeVirtClusterCompatibilityReport compatibilityReport = compatibilityEvaluator.evaluate(
                    runtimeSpec,
                    client.nodes().list().getItems(),
                    isEmulationEnabled(client)
            );
            if (!compatibilityReport.compatible()) {
                return failedDeployment(runtimeSpec, compatibilityReport.message(), compatibilityReport.asDetails());
            }
            ensureNamespace(client, runtimeSpec.namespace());
            client.secrets().inNamespace(runtimeSpec.namespace()).resource(buildCloudInitSecret(runtimeSpec)).createOrReplace();
            client.genericKubernetesResources(VIRTUAL_MACHINE_CONTEXT)
                    .inNamespace(runtimeSpec.namespace())
                    .resource(buildVirtualMachine(runtimeSpec, true))
                    .createOrReplace();
            client.services().inNamespace(runtimeSpec.namespace()).resource(buildService(runtimeSpec)).createOrReplace();
            client.network().v1().ingresses().inNamespace(runtimeSpec.namespace()).resource(buildIngress(runtimeSpec)).createOrReplace();
            TenantSiteRuntimeStatus runtimeStatus = loadRuntimeStatus(runtimeSpec);
            return new TenantSiteDeploymentResult(
                    true,
                    runtimeSpec.virtualMachineName(),
                    "Tenant runtime applied to KubeVirt successfully.",
                    runtimeStatus,
                    Map.of(
                            "namespace", runtimeSpec.namespace(),
                            "serviceName", runtimeSpec.serviceName(),
                            "ingressHost", runtimeSpec.primaryDomain()
                    ),
                    Instant.now()
            );
        } catch (Exception exception) {
            return failedDeployment(runtimeSpec, exception.getMessage(), Map.of("exceptionType", exception.getClass().getName()));
        }
    }

    @Override
    public TenantSiteRuntimeStatus startRuntime(TenantSiteRuntimeSpec runtimeSpec) {
        return patchRunningState(runtimeSpec, true);
    }

    @Override
    public TenantSiteRuntimeStatus stopRuntime(TenantSiteRuntimeSpec runtimeSpec) {
        return patchRunningState(runtimeSpec, false);
    }

    @Override
    public TenantSiteRuntimeStatus restartRuntime(TenantSiteRuntimeSpec runtimeSpec) {
        patchRunningState(runtimeSpec, false);
        return patchRunningState(runtimeSpec, true);
    }

    @Override
    public TenantSiteRuntimeStatus destroyRuntime(TenantSiteRuntimeSpec runtimeSpec) {
        if (controlPlaneProperties.getKubernetes().isDryRun()) {
            return new TenantSiteRuntimeStatus(
                    TenantSiteRuntimePowerState.DESTROYED,
                    "DESTROYED",
                    runtimeSpec.namespace(),
                    runtimeSpec.virtualMachineName(),
                    runtimeSpec.serviceName(),
                    runtimeSpec.primaryDomain(),
                    "Dry-run runtime destroy completed.",
                    Instant.now(),
                    Map.of()
            );
        }
        try (KubernetesClient client = createClient()) {
            client.network().v1().ingresses().inNamespace(runtimeSpec.namespace()).withName(runtimeSpec.ingressName()).delete();
            client.services().inNamespace(runtimeSpec.namespace()).withName(runtimeSpec.serviceName()).delete();
            client.secrets().inNamespace(runtimeSpec.namespace()).withName(cloudInitSecretName(runtimeSpec)).delete();
            client.genericKubernetesResources(VIRTUAL_MACHINE_CONTEXT)
                    .inNamespace(runtimeSpec.namespace())
                    .withName(runtimeSpec.virtualMachineName())
                    .delete();
            return new TenantSiteRuntimeStatus(
                    TenantSiteRuntimePowerState.DESTROYED,
                    "DESTROYED",
                    runtimeSpec.namespace(),
                    runtimeSpec.virtualMachineName(),
                    runtimeSpec.serviceName(),
                    runtimeSpec.primaryDomain(),
                    "Tenant runtime resources deleted.",
                    Instant.now(),
                    Map.of()
            );
        } catch (Exception exception) {
            return failureStatus(runtimeSpec, exception);
        }
    }

    @Override
    public TenantSiteRuntimeStatus loadRuntimeStatus(TenantSiteRuntimeSpec runtimeSpec) {
        if (controlPlaneProperties.getKubernetes().isDryRun()) {
            return new TenantSiteRuntimeStatus(
                    TenantSiteRuntimePowerState.RUNNING,
                    "DRY_RUN",
                    runtimeSpec.namespace(),
                    runtimeSpec.virtualMachineName(),
                    runtimeSpec.serviceName(),
                    runtimeSpec.primaryDomain(),
                    "Dry-run mode reports the runtime as provisioned.",
                    Instant.now(),
                    Map.of()
            );
        }
        try (KubernetesClient client = createClient()) {
            GenericKubernetesResource virtualMachine = client.genericKubernetesResources(VIRTUAL_MACHINE_CONTEXT)
                    .inNamespace(runtimeSpec.namespace())
                    .withName(runtimeSpec.virtualMachineName())
                    .get();
            if (virtualMachine == null) {
                return new TenantSiteRuntimeStatus(
                        TenantSiteRuntimePowerState.DESTROYED,
                        "MISSING",
                        runtimeSpec.namespace(),
                        runtimeSpec.virtualMachineName(),
                        runtimeSpec.serviceName(),
                        runtimeSpec.primaryDomain(),
                        "KubeVirt virtual machine resource was not found.",
                        Instant.now(),
                        Map.of()
                );
            }
            Map<String, Object> spec = castMap(virtualMachine.getAdditionalProperties().get("spec"));
            Map<String, Object> status = castMap(virtualMachine.getAdditionalProperties().get("status"));
            boolean requestedRunning = Boolean.TRUE.equals(spec.get("running"));
            String printableStatus = stringValue(status.get("printableStatus"));
            String phase = printableStatus != null ? printableStatus : stringValue(status.get("phase"));
            TenantSiteRuntimePowerState powerState = mapPowerState(phase, requestedRunning);
            return new TenantSiteRuntimeStatus(
                    powerState,
                    phase == null ? "UNKNOWN" : phase,
                    runtimeSpec.namespace(),
                    runtimeSpec.virtualMachineName(),
                    runtimeSpec.serviceName(),
                    runtimeSpec.primaryDomain(),
                    "Runtime status synchronized from KubeVirt.",
                    Instant.now(),
                    status
            );
        } catch (Exception exception) {
            return failureStatus(runtimeSpec, exception);
        }
    }

    @Override
    public KubeVirtClusterCompatibilityReport loadClusterCompatibility(TenantSiteRuntimeSpec runtimeSpec) {
        if (controlPlaneProperties.getKubernetes().isDryRun()) {
            return new KubeVirtClusterCompatibilityReport(
                    runtimeSpec.infrastructureProfile(),
                    true,
                    0,
                    0,
                    0,
                    0,
                    false,
                    runtimeSpec.nodeSelector(),
                    "Dry-run mode skips live KubeVirt compatibility validation."
            );
        }
        try (KubernetesClient client = createClient()) {
            return compatibilityEvaluator.evaluate(
                    runtimeSpec,
                    client.nodes().list().getItems(),
                    isEmulationEnabled(client)
            );
        } catch (Exception exception) {
            return new KubeVirtClusterCompatibilityReport(
                    runtimeSpec.infrastructureProfile(),
                    false,
                    0,
                    0,
                    0,
                    0,
                    false,
                    runtimeSpec.nodeSelector(),
                    "Failed to inspect KubeVirt cluster compatibility: " + exception.getMessage()
            );
        }
    }

    private TenantSiteRuntimeStatus patchRunningState(TenantSiteRuntimeSpec runtimeSpec, boolean running) {
        if (controlPlaneProperties.getKubernetes().isDryRun()) {
            return new TenantSiteRuntimeStatus(
                    running ? TenantSiteRuntimePowerState.RUNNING : TenantSiteRuntimePowerState.STOPPED,
                    running ? "RUNNING" : "STOPPED",
                    runtimeSpec.namespace(),
                    runtimeSpec.virtualMachineName(),
                    runtimeSpec.serviceName(),
                    runtimeSpec.primaryDomain(),
                    "Dry-run runtime state patch completed.",
                    Instant.now(),
                    Map.of("running", running)
            );
        }
        try (KubernetesClient client = createClient()) {
            var resourceHandle = client.genericKubernetesResources(VIRTUAL_MACHINE_CONTEXT)
                    .inNamespace(runtimeSpec.namespace())
                    .withName(runtimeSpec.virtualMachineName());
            GenericKubernetesResource virtualMachine = resourceHandle.get();
            if (virtualMachine == null) {
                return new TenantSiteRuntimeStatus(
                        TenantSiteRuntimePowerState.DESTROYED,
                        "MISSING",
                        runtimeSpec.namespace(),
                        runtimeSpec.virtualMachineName(),
                        runtimeSpec.serviceName(),
                        runtimeSpec.primaryDomain(),
                        "KubeVirt virtual machine resource was not found.",
                        Instant.now(),
                        Map.of()
                );
            }
            Map<String, Object> spec = castMap(virtualMachine.getAdditionalProperties().get("spec"));
            spec.put("running", running);
            virtualMachine.setAdditionalProperty("spec", spec);
            client.genericKubernetesResources(VIRTUAL_MACHINE_CONTEXT)
                    .inNamespace(runtimeSpec.namespace())
                    .resource(virtualMachine)
                    .createOrReplace();
            return loadRuntimeStatus(runtimeSpec);
        } catch (Exception exception) {
            return failureStatus(runtimeSpec, exception);
        }
    }

    private KubernetesClient createClient() throws IOException {
        String kubeconfigPath = controlPlaneProperties.getKubernetes().getKubeconfigPath();
        Config config = kubeconfigPath == null || kubeconfigPath.isBlank()
                ? Config.autoConfigure(null)
                : Config.fromKubeconfig(Files.readString(Path.of(kubeconfigPath)));
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    private boolean isEmulationEnabled(KubernetesClient client) {
        GenericKubernetesResource kubeVirt = client.genericKubernetesResources(KUBEVIRT_CONTEXT)
                .inNamespace(controlPlaneProperties.getKubernetes().getKubevirtNamespace())
                .withName(controlPlaneProperties.getKubernetes().getKubevirtResourceName())
                .get();
        if (kubeVirt == null) {
            return false;
        }
        Map<String, Object> spec = castMap(kubeVirt.getAdditionalProperties().get("spec"));
        Map<String, Object> configuration = castMap(spec.get("configuration"));
        Map<String, Object> developerConfiguration = castMap(configuration.get("developerConfiguration"));
        Object useEmulation = developerConfiguration.get("useEmulation");
        if (useEmulation instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(useEmulation));
    }

    private void ensureNamespace(KubernetesClient client, String namespace) {
        client.namespaces()
                .resource(new NamespaceBuilder()
                        .withMetadata(new ObjectMetaBuilder().withName(namespace).build())
                        .build())
                .createOrReplace();
    }

    private Secret buildCloudInitSecret(TenantSiteRuntimeSpec runtimeSpec) {
        return new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(cloudInitSecretName(runtimeSpec))
                        .withNamespace(runtimeSpec.namespace())
                        .withLabels(runtimeSpec.labels())
                        .withAnnotations(runtimeSpec.annotations())
                        .build())
                .withType("Opaque")
                .withStringData(Map.of("userdata", buildCloudInit(runtimeSpec)))
                .build();
    }

    private GenericKubernetesResource buildVirtualMachine(TenantSiteRuntimeSpec runtimeSpec, boolean running) {
        Map<String, Object> templateSpec = new LinkedHashMap<>();
        templateSpec.put("terminationGracePeriodSeconds", 30);
        templateSpec.put("domain", Map.of(
                "cpu", Map.of("cores", runtimeSpec.resources().cpuCores()),
                "resources", Map.of(
                        "requests", Map.of("memory", runtimeSpec.resources().memoryMiB() + "Mi")
                ),
                "devices", Map.of(
                        "disks", List.of(
                                Map.of("name", "rootdisk", "disk", Map.of("bus", "virtio")),
                                Map.of("name", "cloudinitdisk", "disk", Map.of("bus", "virtio"))
                        ),
                        "interfaces", List.of(Map.of(
                                "name", "default",
                                "masquerade", Map.of(),
                                "ports", List.of(Map.of("port", 8080))
                        ))
                )
        ));
        if (!runtimeSpec.nodeSelector().isEmpty()) {
            templateSpec.put("nodeSelector", runtimeSpec.nodeSelector());
        }
        templateSpec.put("networks", List.of(Map.of("name", "default", "pod", Map.of())));
        templateSpec.put("volumes", List.of(
                Map.of("name", "rootdisk", "containerDisk", Map.of("image", runtimeSpec.baseImage())),
                Map.of(
                        "name", "cloudinitdisk",
                        "cloudInitNoCloud", Map.of(
                                "secretRef", Map.of("name", cloudInitSecretName(runtimeSpec))
                        )
                )
        ));

        GenericKubernetesResource virtualMachine = new GenericKubernetesResource();
        virtualMachine.setApiVersion("kubevirt.io/v1");
        virtualMachine.setKind("VirtualMachine");
        virtualMachine.setMetadata(new ObjectMetaBuilder()
                .withName(runtimeSpec.virtualMachineName())
                .withNamespace(runtimeSpec.namespace())
                .withLabels(runtimeSpec.labels())
                .withAnnotations(runtimeSpec.annotations())
                .build());
        virtualMachine.setAdditionalProperty("spec", Map.of(
                "running", running,
                "template", Map.of(
                        "metadata", Map.of("labels", runtimeSpec.labels()),
                        "spec", templateSpec
                )
        ));
        return virtualMachine;
    }

    private io.fabric8.kubernetes.api.model.Service buildService(TenantSiteRuntimeSpec runtimeSpec) {
        return new ServiceBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(runtimeSpec.serviceName())
                        .withNamespace(runtimeSpec.namespace())
                        .withLabels(runtimeSpec.labels())
                        .build())
                .withNewSpec()
                .withSelector(runtimeSpec.labels())
                .addNewPort()
                .withName("http")
                .withPort(80)
                .withTargetPort(new IntOrString(8080))
                .endPort()
                .withType("ClusterIP")
                .endSpec()
                .build();
    }

    private Ingress buildIngress(TenantSiteRuntimeSpec runtimeSpec) {
        return new IngressBuilder()
                .withMetadata(new ObjectMetaBuilder()
                        .withName(runtimeSpec.ingressName())
                        .withNamespace(runtimeSpec.namespace())
                        .withLabels(runtimeSpec.labels())
                        .addToAnnotations("nginx.ingress.kubernetes.io/proxy-body-size", "25m")
                        .build())
                .withNewSpec()
                .withIngressClassName(controlPlaneProperties.getKubernetes().getIngressClassName())
                .withRules(new io.fabric8.kubernetes.api.model.networking.v1.IngressRuleBuilder()
                        .withHost(runtimeSpec.primaryDomain())
                        .withNewHttp()
                        .withPaths(new HTTPIngressPathBuilder()
                                .withPath("/")
                                .withPathType("Prefix")
                                .withBackend(new io.fabric8.kubernetes.api.model.networking.v1.IngressBackendBuilder()
                                        .withService(new IngressServiceBackendBuilder()
                                                .withName(runtimeSpec.serviceName())
                                                .withPort(new io.fabric8.kubernetes.api.model.networking.v1.ServiceBackendPortBuilder()
                                                        .withNumber(80)
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .endHttp()
                        .build())
                .endSpec()
                .build();
    }

    private String buildCloudInit(TenantSiteRuntimeSpec runtimeSpec) {
        StringBuilder envFile = new StringBuilder();
        runtimeSpec.environment().forEach((key, value) -> envFile.append(key).append("=").append(value).append("\n"));
        String bootstrapScript = buildBootstrapScript(runtimeSpec);
        return """
                #cloud-config
                write_files:
                  - path: /etc/webstore-view/runtime.env
                    permissions: '0644'
                    content: |
                """
                + indent(envFile.toString(), "      ")
                + """
                  - path: /usr/local/bin/platform-bootstrap-webstore.sh
                    permissions: '0755'
                    content: |
                """
                + indent(bootstrapScript, "      ")
                + """

                runcmd:
                  - [ bash, -lc, "/usr/local/bin/platform-bootstrap-webstore.sh" ]
                """;
    }

    private String buildBootstrapScript(TenantSiteRuntimeSpec runtimeSpec) {
        if (!StringUtils.hasText(runtimeSpec.bootstrapArtifactUrl())) {
            return """
                    #!/usr/bin/env bash
                    set -euo pipefail
                    mkdir -p /etc/webstore-view /srv/webstore/media /srv/webstore/theme
                    systemctl daemon-reload || true
                    systemctl restart webstore-view || true
                    """;
        }
        String checksumCommand = StringUtils.hasText(runtimeSpec.bootstrapArtifactSha256())
                ? "echo \"" + escapeForShell(runtimeSpec.bootstrapArtifactSha256()) + "  /opt/webstore-view/webstore-view.jar\" | sha256sum -c -"
                : "echo 'No bootstrap checksum configured; skipping artifact verification.'";
        return """
                #!/usr/bin/env bash
                set -euo pipefail
                if command -v dnf >/dev/null 2>&1; then
                  dnf install -y curl java-21-openjdk-headless
                elif command -v apt-get >/dev/null 2>&1; then
                  export DEBIAN_FRONTEND=noninteractive
                  apt-get update
                  apt-get install -y curl openjdk-21-jre-headless
                fi
                mkdir -p /etc/webstore-view /opt/webstore-view /srv/webstore/media /srv/webstore/theme
                curl -fsSL "%s" -o /opt/webstore-view/webstore-view.jar
                %s
                cat > /etc/systemd/system/webstore-view.service <<'SERVICE'
                [Unit]
                Description=Tavall Webstore Runtime
                After=network-online.target
                Wants=network-online.target

                [Service]
                Type=simple
                WorkingDirectory=/opt/webstore-view
                EnvironmentFile=/etc/webstore-view/runtime.env
                ExecStart=/usr/bin/env java -jar /opt/webstore-view/webstore-view.jar
                Restart=always
                RestartSec=5

                [Install]
                WantedBy=multi-user.target
                SERVICE
                systemctl daemon-reload
                systemctl enable --now webstore-view
                """.formatted(escapeForShell(runtimeSpec.bootstrapArtifactUrl()), checksumCommand);
    }

    private String indent(String value, String prefix) {
        String[] lines = value.split("\\R");
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (!line.isBlank()) {
                builder.append(prefix).append(line).append("\n");
            }
        }
        return builder.toString();
    }

    private TenantSiteDeploymentResult dryRunResult(TenantSiteRuntimeSpec runtimeSpec, String message) {
        return new TenantSiteDeploymentResult(
                true,
                runtimeSpec.virtualMachineName(),
                message,
                new TenantSiteRuntimeStatus(
                        TenantSiteRuntimePowerState.PROVISIONING,
                        "DRY_RUN",
                        runtimeSpec.namespace(),
                        runtimeSpec.virtualMachineName(),
                        runtimeSpec.serviceName(),
                        runtimeSpec.primaryDomain(),
                        message,
                        Instant.now(),
                        Map.of("dryRun", true)
                ),
                Map.of(
                        "cloudInitSecret", buildCloudInitSecret(runtimeSpec).getMetadata().getName(),
                        "virtualMachineManifest", buildVirtualMachine(runtimeSpec, true).getAdditionalProperties(),
                        "serviceName", runtimeSpec.serviceName()
                ),
                Instant.now()
        );
    }

    private String cloudInitSecretName(TenantSiteRuntimeSpec runtimeSpec) {
        return runtimeSpec.virtualMachineName() + "-cloudinit";
    }

    private TenantSiteRuntimePowerState mapPowerState(String phase, boolean requestedRunning) {
        if (phase == null) {
            return requestedRunning ? TenantSiteRuntimePowerState.PROVISIONING : TenantSiteRuntimePowerState.STOPPED;
        }
        String normalized = phase.toUpperCase();
        if (normalized.contains("CRASH") || normalized.contains("BACKOFF")) {
            return TenantSiteRuntimePowerState.FAILED;
        }
        if (normalized.contains("RUN")) {
            return TenantSiteRuntimePowerState.RUNNING;
        }
        if (normalized.contains("STOP")) {
            return TenantSiteRuntimePowerState.STOPPED;
        }
        if (normalized.contains("FAIL") || normalized.contains("ERROR")) {
            return TenantSiteRuntimePowerState.FAILED;
        }
        if (normalized.contains("PROVISION") || normalized.contains("START")) {
            return TenantSiteRuntimePowerState.PROVISIONING;
        }
        return requestedRunning ? TenantSiteRuntimePowerState.UNKNOWN : TenantSiteRuntimePowerState.STOPPED;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> cast = new LinkedHashMap<>();
            source.forEach((key, item) -> cast.put(String.valueOf(key), item));
            return cast;
        }
        return new LinkedHashMap<>();
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String escapeForShell(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private TenantSiteDeploymentResult failedDeployment(
            TenantSiteRuntimeSpec runtimeSpec,
            String message,
            Map<String, Object> details
    ) {
        return new TenantSiteDeploymentResult(
                false,
                runtimeSpec.virtualMachineName(),
                message,
                new TenantSiteRuntimeStatus(
                        TenantSiteRuntimePowerState.FAILED,
                        "FAILED",
                        runtimeSpec.namespace(),
                        runtimeSpec.virtualMachineName(),
                        runtimeSpec.serviceName(),
                        runtimeSpec.primaryDomain(),
                        message,
                        Instant.now(),
                        details
                ),
                details,
                Instant.now()
        );
    }

    private TenantSiteRuntimeStatus failureStatus(TenantSiteRuntimeSpec runtimeSpec, Exception exception) {
        return new TenantSiteRuntimeStatus(
                TenantSiteRuntimePowerState.FAILED,
                "FAILED",
                runtimeSpec.namespace(),
                runtimeSpec.virtualMachineName(),
                runtimeSpec.serviceName(),
                runtimeSpec.primaryDomain(),
                exception.getMessage(),
                Instant.now(),
                Map.of("exceptionType", exception.getClass().getName())
        );
    }
}
