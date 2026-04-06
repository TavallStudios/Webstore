package org.tavall.webstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storefront")
public class StorefrontProperties {

    private final Media media = new Media();
    private final Assets assets = new Assets();
    private final Theme theme = new Theme();
    private final Runtime runtime = new Runtime();

    public Media getMedia() {
        return media;
    }

    public Assets getAssets() {
        return assets;
    }

    public Theme getTheme() {
        return theme;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public static class Media {
        private String storagePath = "./media";

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }
    }

    public static class Assets {
        private int mediaCacheDays = 30;
        private int staticCacheDays = 365;
        private String devServerUrl = "";

        public int getMediaCacheDays() {
            return mediaCacheDays;
        }

        public void setMediaCacheDays(int mediaCacheDays) {
            this.mediaCacheDays = mediaCacheDays;
        }

        public int getStaticCacheDays() {
            return staticCacheDays;
        }

        public void setStaticCacheDays(int staticCacheDays) {
            this.staticCacheDays = staticCacheDays;
        }

        public String getDevServerUrl() {
            return devServerUrl;
        }

        public void setDevServerUrl(String devServerUrl) {
            this.devServerUrl = devServerUrl;
        }
    }

    public static class Theme {
        private String storagePath = "./media/themes/active";

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
        }
    }

    public static class Runtime {
        private String tenantAccountId = "";
        private String siteId = "";
        private String siteSlug = "default";
        private String publicationVersion = "v1";
        private String primaryDomain = "localhost";
        private String databaseSchema = "public";

        public String getTenantAccountId() {
            return tenantAccountId;
        }

        public void setTenantAccountId(String tenantAccountId) {
            this.tenantAccountId = tenantAccountId;
        }

        public String getSiteId() {
            return siteId;
        }

        public void setSiteId(String siteId) {
            this.siteId = siteId;
        }

        public String getSiteSlug() {
            return siteSlug;
        }

        public void setSiteSlug(String siteSlug) {
            this.siteSlug = siteSlug;
        }

        public String getPublicationVersion() {
            return publicationVersion;
        }

        public void setPublicationVersion(String publicationVersion) {
            this.publicationVersion = publicationVersion;
        }

        public String getPrimaryDomain() {
            return primaryDomain;
        }

        public void setPrimaryDomain(String primaryDomain) {
            this.primaryDomain = primaryDomain;
        }

        public String getDatabaseSchema() {
            return databaseSchema;
        }

        public void setDatabaseSchema(String databaseSchema) {
            this.databaseSchema = databaseSchema;
        }
    }
}
