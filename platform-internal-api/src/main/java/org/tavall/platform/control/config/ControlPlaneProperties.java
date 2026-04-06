package org.tavall.platform.control.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;

@ConfigurationProperties(prefix = "platform.control")
public class ControlPlaneProperties {

    private final InternalApi internalApi = new InternalApi();
    private final Kubernetes kubernetes = new Kubernetes();
    private final Routing routing = new Routing();
    private final TenantRuntime tenantRuntime = new TenantRuntime();
    private final Sync sync = new Sync();

    public InternalApi getInternalApi() {
        return internalApi;
    }

    public Kubernetes getKubernetes() {
        return kubernetes;
    }

    public Routing getRouting() {
        return routing;
    }

    public TenantRuntime getTenantRuntime() {
        return tenantRuntime;
    }

    public Sync getSync() {
        return sync;
    }

    public static class InternalApi {
        private String sharedSecret = "change-me";

        public String getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(String sharedSecret) {
            this.sharedSecret = sharedSecret;
        }
    }

    public static class Kubernetes {
        private String kubeconfigPath = "";
        private String kubevirtNamespace = "kubevirt";
        private String kubevirtResourceName = "kubevirt";
        private String namespacePrefix = "tenant-site";
        private String runtimeBaseImage = "ghcr.io/tavall/webstore-view:latest";
        private String ingressClassName = "nginx";
        private TenantRuntimeInfrastructureProfile infrastructureProfile = TenantRuntimeInfrastructureProfile.AUTO;
        private String dedicatedNodeSelector = "platform.tavall/runtime-profile=dedicated-kubevirt";
        private String nestedNodeSelector = "platform.tavall/runtime-profile=nested-kubevirt";
        private boolean dryRun;

        public String getKubeconfigPath() {
            return kubeconfigPath;
        }

        public void setKubeconfigPath(String kubeconfigPath) {
            this.kubeconfigPath = kubeconfigPath;
        }

        public String getKubevirtNamespace() {
            return kubevirtNamespace;
        }

        public void setKubevirtNamespace(String kubevirtNamespace) {
            this.kubevirtNamespace = kubevirtNamespace;
        }

        public String getKubevirtResourceName() {
            return kubevirtResourceName;
        }

        public void setKubevirtResourceName(String kubevirtResourceName) {
            this.kubevirtResourceName = kubevirtResourceName;
        }

        public String getNamespacePrefix() {
            return namespacePrefix;
        }

        public void setNamespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
        }

        public String getRuntimeBaseImage() {
            return runtimeBaseImage;
        }

        public void setRuntimeBaseImage(String runtimeBaseImage) {
            this.runtimeBaseImage = runtimeBaseImage;
        }

        public String getIngressClassName() {
            return ingressClassName;
        }

        public void setIngressClassName(String ingressClassName) {
            this.ingressClassName = ingressClassName;
        }

        public TenantRuntimeInfrastructureProfile getInfrastructureProfile() {
            return infrastructureProfile;
        }

        public void setInfrastructureProfile(TenantRuntimeInfrastructureProfile infrastructureProfile) {
            this.infrastructureProfile = infrastructureProfile;
        }

        public String getDedicatedNodeSelector() {
            return dedicatedNodeSelector;
        }

        public void setDedicatedNodeSelector(String dedicatedNodeSelector) {
            this.dedicatedNodeSelector = dedicatedNodeSelector;
        }

        public String getNestedNodeSelector() {
            return nestedNodeSelector;
        }

        public void setNestedNodeSelector(String nestedNodeSelector) {
            this.nestedNodeSelector = nestedNodeSelector;
        }

        public boolean isDryRun() {
            return dryRun;
        }

        public void setDryRun(boolean dryRun) {
            this.dryRun = dryRun;
        }
    }

    public static class Routing {
        private String publicDomainSuffix = "stores.local";

        public String getPublicDomainSuffix() {
            return publicDomainSuffix;
        }

        public void setPublicDomainSuffix(String publicDomainSuffix) {
            this.publicDomainSuffix = publicDomainSuffix;
        }
    }

    public static class TenantRuntime {
        private String databaseUrl = "jdbc:postgresql://postgres:5432/tavall";
        private String databaseUsername = "novus";
        private String databasePassword = "";
        private String mediaStoragePath = "/srv/webstore/media";
        private String themeStoragePath = "/srv/webstore/theme";
        private String bootstrapArtifactUrl = "";
        private String bootstrapArtifactSha256 = "";

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        public void setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
        }

        public String getDatabaseUsername() {
            return databaseUsername;
        }

        public void setDatabaseUsername(String databaseUsername) {
            this.databaseUsername = databaseUsername;
        }

        public String getDatabasePassword() {
            return databasePassword;
        }

        public void setDatabasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
        }

        public String getMediaStoragePath() {
            return mediaStoragePath;
        }

        public void setMediaStoragePath(String mediaStoragePath) {
            this.mediaStoragePath = mediaStoragePath;
        }

        public String getThemeStoragePath() {
            return themeStoragePath;
        }

        public void setThemeStoragePath(String themeStoragePath) {
            this.themeStoragePath = themeStoragePath;
        }

        public String getBootstrapArtifactUrl() {
            return bootstrapArtifactUrl;
        }

        public void setBootstrapArtifactUrl(String bootstrapArtifactUrl) {
            this.bootstrapArtifactUrl = bootstrapArtifactUrl;
        }

        public String getBootstrapArtifactSha256() {
            return bootstrapArtifactSha256;
        }

        public void setBootstrapArtifactSha256(String bootstrapArtifactSha256) {
            this.bootstrapArtifactSha256 = bootstrapArtifactSha256;
        }
    }

    public static class Sync {
        private long fixedDelayMs = 30000L;

        public long getFixedDelayMs() {
            return fixedDelayMs;
        }

        public void setFixedDelayMs(long fixedDelayMs) {
            this.fixedDelayMs = fixedDelayMs;
        }
    }
}
