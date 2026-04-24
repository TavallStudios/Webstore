package org.tavall.webstore.content.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.admin.input.PaymentSettingsAdminInput;
import org.tavall.webstore.admin.input.SiteSettingsAdminInput;
import org.tavall.webstore.content.model.SiteSettings;
import org.tavall.webstore.content.repository.SiteSettingsRepository;

@Service
public class SiteSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;
    private final JsonContentService jsonContentService;

    public SiteSettingsService(SiteSettingsRepository siteSettingsRepository, JsonContentService jsonContentService) {
        this.siteSettingsRepository = siteSettingsRepository;
        this.jsonContentService = jsonContentService;
    }

    @Transactional(readOnly = true)
    public SiteSettings getCurrentSettings() {
        List<SiteSettings> siteSettingsList = siteSettingsRepository.findAll();
        if (!siteSettingsList.isEmpty()) {
            return siteSettingsList.getFirst();
        }

        SiteSettings defaults = new SiteSettings();
        defaults.setSiteName("Storefront");
        defaults.setSupportEmail("support@example.test");
        defaults.setHeaderCtaText("Shop now");
        defaults.setBrandPalette(new HashMap<>(Map.of(
                "background", "#ffffff",
                "surface", "#f7f7f7",
                "ink", "#1a1a1a",
                "accent", "#d15a2b",
                "accentDeep", "#8a2d15",
                "muted", "#666666"
        )));
        defaults.setTypography(new HashMap<>(Map.of(
                "headline", "Fraunces, Georgia, serif",
                "body", "IBM Plex Sans, Segoe UI, sans-serif"
        )));
        defaults.setSeoDefaults(new HashMap<>(Map.of("titleTemplate", "%s | Storefront")));
        defaults.setPaymentSettings(new HashMap<>(Map.of(
                "stripeEnabled", true,
                "paypalEnabled", true,
                "mode", "sandbox",
                "defaultProvider", "STRIPE",
                "providerOrder", List.of("STRIPE", "PAYPAL"),
                "manualReviewEnabled", true,
                "saveCardsEnabled", true,
                "sandboxNotice", "Hosted checkout runs in sandbox until production credentials are connected."
        )));
        return defaults;
    }

    @Transactional
    public SiteSettings updateSiteSettings(SiteSettingsAdminInput input) {
        SiteSettings siteSettings = siteSettingsRepository.findAll().stream().findFirst().orElseGet(SiteSettings::new);
        siteSettings.setSiteName(input.getSiteName());
        siteSettings.setLogoPath(input.getLogoPath());
        siteSettings.setFaviconPath(input.getFaviconPath());
        siteSettings.setSupportEmail(input.getSupportEmail());
        siteSettings.setHeaderCtaText(input.getHeaderCtaText());
        siteSettings.setShippingMessage(input.getShippingMessage());
        siteSettings.setReturnMessage(input.getReturnMessage());
        siteSettings.setGuaranteeMessage(input.getGuaranteeMessage());
        siteSettings.setBrandPalette(new HashMap<>(jsonContentService.parseObject(input.getBrandPaletteJson())));
        siteSettings.setTypography(new HashMap<>(jsonContentService.parseObject(input.getTypographyJson())));
        siteSettings.setSocialLinks(new HashMap<>(jsonContentService.parseObject(input.getSocialLinksJson())));
        siteSettings.setSeoDefaults(new HashMap<>(jsonContentService.parseObject(input.getSeoDefaultsJson())));
        siteSettings.setAnalyticsSettings(new HashMap<>(jsonContentService.parseObject(input.getAnalyticsSettingsJson())));
        siteSettings.setPaymentSettings(new HashMap<>(jsonContentService.parseObject(input.getPaymentSettingsJson())));
        siteSettings.setReviewSource(new HashMap<>(jsonContentService.parseObject(input.getReviewSourceJson())));
        siteSettings.setFooterContent(new HashMap<>(jsonContentService.parseObject(input.getFooterContentJson())));
        siteSettings.setAnnouncementBars(jsonContentService.parseObjectList(input.getAnnouncementBarsJson()));
        siteSettings.setPromoBanners(jsonContentService.parseObjectList(input.getPromoBannersJson()));
        siteSettings.setTrustBadges(jsonContentService.parseObjectList(input.getTrustBadgesJson()));
        return siteSettingsRepository.save(siteSettings);
    }

    @Transactional
    public SiteSettings updatePaymentSettings(PaymentSettingsAdminInput input) {
        SiteSettings siteSettings = siteSettingsRepository.findAll().stream().findFirst().orElseGet(this::getCurrentSettings);
        Map<String, Object> paymentSettings = new HashMap<>(siteSettings.getPaymentSettings());
        boolean stripeEnabled = input.isStripeEnabled();
        boolean paypalEnabled = input.isPaypalEnabled();
        if (!stripeEnabled && !paypalEnabled) {
            stripeEnabled = true;
        }

        paymentSettings.put("mode", defaultText(input.getMode(), "sandbox"));
        paymentSettings.put("defaultProvider", defaultText(resolveDefaultProvider(input, stripeEnabled, paypalEnabled), "STRIPE"));
        paymentSettings.put("sandboxNotice", defaultText(
                input.getSandboxNotice(),
                "Hosted checkout runs in sandbox until production credentials are connected."
        ));
        paymentSettings.put("stripeEnabled", stripeEnabled);
        paymentSettings.put("stripePublishableKey", trimToNull(input.getStripePublishableKey()));
        preserveSecret(paymentSettings, "stripeSecretKey", input.getStripeSecretKey());
        preserveSecret(paymentSettings, "stripeWebhookSecret", input.getStripeWebhookSecret());
        paymentSettings.put("stripeAccountId", trimToNull(input.getStripeAccountId()));
        paymentSettings.put("stripeStatementDescriptor", trimToNull(input.getStripeStatementDescriptor()));
        paymentSettings.put("paypalEnabled", paypalEnabled);
        paymentSettings.put("paypalClientId", trimToNull(input.getPaypalClientId()));
        preserveSecret(paymentSettings, "paypalSecret", input.getPaypalSecret());
        paymentSettings.put("paypalWebhookId", trimToNull(input.getPaypalWebhookId()));
        paymentSettings.put("paypalMerchantId", trimToNull(input.getPaypalMerchantId()));
        paymentSettings.put("paypalStatementDescriptor", trimToNull(input.getPaypalStatementDescriptor()));
        paymentSettings.put("manualReviewEnabled", input.isManualReviewEnabled());
        paymentSettings.put("saveCardsEnabled", input.isSaveCardsEnabled());
        paymentSettings.put("providerOrder", buildProviderOrder(input, stripeEnabled, paypalEnabled));

        siteSettings.setPaymentSettings(paymentSettings);
        return siteSettingsRepository.save(siteSettings);
    }

    public String maskSecret(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return "";
        }
        String rawValue = String.valueOf(value);
        String suffix = rawValue.length() <= 4 ? rawValue : rawValue.substring(rawValue.length() - 4);
        return "Saved (" + suffix + ")";
    }

    private void preserveSecret(Map<String, Object> paymentSettings, String key, String submittedValue) {
        if (submittedValue != null && !submittedValue.isBlank()) {
            paymentSettings.put(key, submittedValue.trim());
        }
    }

    private List<String> buildProviderOrder(PaymentSettingsAdminInput input, boolean stripeEnabled, boolean paypalEnabled) {
        java.util.ArrayList<String> providerOrder = new java.util.ArrayList<>();
        String preferredProvider = defaultText(resolveDefaultProvider(input, stripeEnabled, paypalEnabled), "STRIPE");
        if ("PAYPAL".equalsIgnoreCase(preferredProvider) && paypalEnabled) {
            providerOrder.add("PAYPAL");
        }
        if ("STRIPE".equalsIgnoreCase(preferredProvider) && stripeEnabled) {
            providerOrder.add("STRIPE");
        }
        if (!providerOrder.contains("STRIPE") && stripeEnabled) {
            providerOrder.add("STRIPE");
        }
        if (!providerOrder.contains("PAYPAL") && paypalEnabled) {
            providerOrder.add("PAYPAL");
        }
        if (providerOrder.isEmpty()) {
            providerOrder.add("STRIPE");
        }
        return providerOrder;
    }

    private String resolveDefaultProvider(PaymentSettingsAdminInput input, boolean stripeEnabled, boolean paypalEnabled) {
        if (input.getDefaultProvider() != null && !input.getDefaultProvider().isBlank()) {
            return input.getDefaultProvider().trim().toUpperCase();
        }
        if (stripeEnabled) {
            return "STRIPE";
        }
        if (paypalEnabled) {
            return "PAYPAL";
        }
        return "STRIPE";
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
