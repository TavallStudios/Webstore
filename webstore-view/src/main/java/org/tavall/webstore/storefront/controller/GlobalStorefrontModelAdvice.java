package org.tavall.webstore.storefront.controller;

import java.lang.management.ManagementFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.cart.service.CartSessionService;
import org.tavall.webstore.config.StorefrontProperties;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.theme.service.ThemeAssetService;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalStorefrontModelAdvice {

    private final String assetVersion;
    private final String devAssetOrigin;
    private final StorefrontProperties storefrontProperties;
    private final SiteSettingsService siteSettingsService;
    private final FeatureFlagService featureFlagService;
    private final CartService cartService;
    private final CartSessionService cartSessionService;
    private final ThemeAssetService themeAssetService;

    public GlobalStorefrontModelAdvice(
            StorefrontProperties storefrontProperties,
            SiteSettingsService siteSettingsService,
            FeatureFlagService featureFlagService,
            CartService cartService,
            CartSessionService cartSessionService,
            ThemeAssetService themeAssetService
    ) {
        this.assetVersion = Long.toString(ManagementFactory.getRuntimeMXBean().getStartTime());
        this.storefrontProperties = storefrontProperties;
        this.devAssetOrigin = normalizeDevAssetOrigin(
                storefrontProperties != null && storefrontProperties.getAssets() != null
                        ? storefrontProperties.getAssets().getDevServerUrl()
                        : null
        );
        this.siteSettingsService = siteSettingsService;
        this.featureFlagService = featureFlagService;
        this.cartService = cartService;
        this.cartSessionService = cartSessionService;
        this.themeAssetService = themeAssetService;
    }

    @ModelAttribute("globalSiteSettings")
    public Object globalSiteSettings() {
        return siteSettingsService.getCurrentSettings();
    }

    @ModelAttribute("globalFeatureFlags")
    public Object globalFeatureFlags() {
        return featureFlagService.getFeatureFlagMap();
    }

    @ModelAttribute("globalCartItemCount")
    public int globalCartItemCount(HttpSession session) {
        String cartToken = cartSessionService.getExistingCartToken(session);
        return cartService.getCartItemCount(cartToken);
    }

    @ModelAttribute("globalThemeRender")
    public Object globalThemeRender() {
        return themeAssetService.buildRenderView();
    }

    @ModelAttribute("globalAssetVersion")
    public String globalAssetVersion() {
        return assetVersion;
    }

    @ModelAttribute("globalDevAssetOrigin")
    public String globalDevAssetOrigin() {
        return devAssetOrigin;
    }

    @ModelAttribute("globalRuntimeIdentity")
    public Object globalRuntimeIdentity() {
        return storefrontProperties.getRuntime();
    }

    private String normalizeDevAssetOrigin(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
