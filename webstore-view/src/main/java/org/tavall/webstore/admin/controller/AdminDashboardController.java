package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.tavall.webstore.catalog.repository.ProductRepository;
import org.tavall.webstore.content.repository.ContentPageRepository;
import org.tavall.webstore.content.repository.PageSectionRepository;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.media.service.MediaAssetService;
import org.tavall.webstore.orders.service.OrderOperationsService;
import org.tavall.webstore.storefront.view.AdminDashboardView;
import org.tavall.webstore.theme.service.ThemeAssetService;

@Controller
public class AdminDashboardController {

    private final ProductRepository productRepository;
    private final ContentPageRepository contentPageRepository;
    private final PageSectionRepository pageSectionRepository;
    private final FeatureFlagService featureFlagService;
    private final SiteSettingsService siteSettingsService;
    private final OrderOperationsService orderOperationsService;
    private final MediaAssetService mediaAssetService;
    private final ThemeAssetService themeAssetService;

    public AdminDashboardController(
            ProductRepository productRepository,
            ContentPageRepository contentPageRepository,
            PageSectionRepository pageSectionRepository,
            FeatureFlagService featureFlagService,
            SiteSettingsService siteSettingsService,
            OrderOperationsService orderOperationsService,
            MediaAssetService mediaAssetService,
            ThemeAssetService themeAssetService
    ) {
        this.productRepository = productRepository;
        this.contentPageRepository = contentPageRepository;
        this.pageSectionRepository = pageSectionRepository;
        this.featureFlagService = featureFlagService;
        this.siteSettingsService = siteSettingsService;
        this.orderOperationsService = orderOperationsService;
        this.mediaAssetService = mediaAssetService;
        this.themeAssetService = themeAssetService;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        var siteSettings = siteSettingsService.getCurrentSettings();
        java.util.Map<String, Object> paymentSettings = siteSettings.getPaymentSettings();
        var themeRenderView = themeAssetService.buildRenderView();
        AdminDashboardView dashboardView = new AdminDashboardView(
                siteSettings.getSiteName(),
                productRepository.count(),
                featureFlagService.listFeatureFlags().stream().filter(flag -> flag.isEnabled()).count(),
                orderOperationsService.listOrders().size(),
                mediaAssetService.listAssets().size(),
                contentPageRepository.count(),
                pageSectionRepository.count(),
                themeAssetService.countFiles(),
                countConfiguredProviders(paymentSettings),
                String.valueOf(paymentSettings.getOrDefault("mode", "sandbox")),
                themeRenderView.cssAvailable() || themeRenderView.scriptAvailable()
        );
        model.addAttribute("dashboard", dashboardView);
        return "admin/dashboard";
    }

    private long countConfiguredProviders(java.util.Map<String, Object> paymentSettings) {
        long configuredProviders = 0;
        if (isStripeConfigured(paymentSettings)) {
            configuredProviders++;
        }
        if (isPaypalConfigured(paymentSettings)) {
            configuredProviders++;
        }
        return configuredProviders;
    }

    private boolean isStripeConfigured(java.util.Map<String, Object> paymentSettings) {
        return Boolean.TRUE.equals(paymentSettings.get("stripeEnabled"))
                && hasValue(paymentSettings.get("stripePublishableKey"))
                && hasValue(paymentSettings.get("stripeSecretKey"));
    }

    private boolean isPaypalConfigured(java.util.Map<String, Object> paymentSettings) {
        return Boolean.TRUE.equals(paymentSettings.get("paypalEnabled"))
                && hasValue(paymentSettings.get("paypalClientId"))
                && hasValue(paymentSettings.get("paypalSecret"));
    }

    private boolean hasValue(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }
}
