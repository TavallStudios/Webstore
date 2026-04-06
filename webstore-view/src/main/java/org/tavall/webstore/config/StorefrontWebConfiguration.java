package org.tavall.webstore.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorefrontWebConfiguration implements WebMvcConfigurer {

    private final StorefrontProperties storefrontProperties;

    public StorefrontWebConfiguration(StorefrontProperties storefrontProperties) {
        this.storefrontProperties = storefrontProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        StorefrontProperties.Assets assets = storefrontProperties != null && storefrontProperties.getAssets() != null
                ? storefrontProperties.getAssets()
                : new StorefrontProperties.Assets();
        StorefrontProperties.Media media = storefrontProperties != null && storefrontProperties.getMedia() != null
                ? storefrontProperties.getMedia()
                : new StorefrontProperties.Media();
        StorefrontProperties.Theme theme = storefrontProperties != null && storefrontProperties.getTheme() != null
                ? storefrontProperties.getTheme()
                : new StorefrontProperties.Theme();

        registry.addResourceHandler("/assets/storefront/**")
                .addResourceLocations("classpath:/static/assets/storefront/")
                .setCacheControl(CacheControl.maxAge(assets.getStaticCacheDays(), TimeUnit.DAYS)
                        .cachePublic()
                        .immutable());

        registry.addResourceHandler("/assets/admin/**")
                .addResourceLocations("classpath:/static/assets/admin/")
                .setCacheControl(CacheControl.noStore());

        registry.addResourceHandler("/assets/core/**")
                .addResourceLocations("classpath:/static/assets/core/")
                .setCacheControl(CacheControl.noCache().cachePublic());

        Path mediaPath = Paths.get(media.getStoragePath()).toAbsolutePath().normalize();
        registry.addResourceHandler("/media/**")
                .addResourceLocations(mediaPath.toUri().toString())
                .setCacheControl(CacheControl.maxAge(assets.getMediaCacheDays(), TimeUnit.DAYS)
                        .cachePublic());

        Path themePath = Paths.get(theme.getStoragePath()).toAbsolutePath().normalize();
        registry.addResourceHandler("/theme-assets/**")
                .addResourceLocations(themePath.toUri().toString())
                .setCacheControl(CacheControl.noCache().cachePublic());
    }
}
