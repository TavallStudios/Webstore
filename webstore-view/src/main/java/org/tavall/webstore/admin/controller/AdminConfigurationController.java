package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.tavall.webstore.admin.input.PaymentSettingsAdminInput;
import org.tavall.webstore.admin.input.SiteSettingsAdminInput;
import org.tavall.webstore.content.model.SiteSettings;
import org.tavall.webstore.content.service.JsonContentService;
import org.tavall.webstore.content.service.SiteSettingsService;

@Controller
public class AdminConfigurationController {

    private final SiteSettingsService siteSettingsService;
    private final JsonContentService jsonContentService;

    public AdminConfigurationController(SiteSettingsService siteSettingsService, JsonContentService jsonContentService) {
        this.siteSettingsService = siteSettingsService;
        this.jsonContentService = jsonContentService;
    }

    @GetMapping("/admin/site-settings")
    public String siteSettings(Model model) {
        model.addAttribute("siteSettingsInput", toInput(siteSettingsService.getCurrentSettings()));
        return "admin/site-settings";
    }

    @PostMapping("/admin/site-settings")
    public String saveSiteSettings(@ModelAttribute SiteSettingsAdminInput siteSettingsAdminInput) {
        siteSettingsService.updateSiteSettings(siteSettingsAdminInput);
        return "redirect:/admin/site-settings?saved=1";
    }

    @GetMapping("/admin/payments")
    public String paymentSettings(Model model) {
        SiteSettings siteSettings = siteSettingsService.getCurrentSettings();
        model.addAttribute("paymentSettingsInput", toPaymentInput(siteSettings));
        model.addAttribute("maskedStripeSecret", siteSettingsService.maskSecret(siteSettings.getPaymentSettings().get("stripeSecretKey")));
        model.addAttribute("maskedStripeWebhookSecret", siteSettingsService.maskSecret(siteSettings.getPaymentSettings().get("stripeWebhookSecret")));
        model.addAttribute("maskedPaypalSecret", siteSettingsService.maskSecret(siteSettings.getPaymentSettings().get("paypalSecret")));
        return "admin/payment-settings";
    }

    @PostMapping("/admin/payments")
    public String savePaymentSettings(@ModelAttribute PaymentSettingsAdminInput paymentSettingsAdminInput) {
        siteSettingsService.updatePaymentSettings(paymentSettingsAdminInput);
        return "redirect:/admin/payments?saved=1";
    }

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        model.addAttribute("siteSettingsInput", toInput(siteSettingsService.getCurrentSettings()));
        return "admin/analytics";
    }

    @PostMapping("/admin/analytics")
    public String saveAnalytics(@ModelAttribute SiteSettingsAdminInput siteSettingsAdminInput) {
        SiteSettings currentSettings = siteSettingsService.getCurrentSettings();
        siteSettingsAdminInput.setSiteName(currentSettings.getSiteName());
        siteSettingsAdminInput.setLogoPath(currentSettings.getLogoPath());
        siteSettingsAdminInput.setFaviconPath(currentSettings.getFaviconPath());
        siteSettingsAdminInput.setSupportEmail(currentSettings.getSupportEmail());
        siteSettingsAdminInput.setHeaderCtaText(currentSettings.getHeaderCtaText());
        siteSettingsAdminInput.setShippingMessage(currentSettings.getShippingMessage());
        siteSettingsAdminInput.setReturnMessage(currentSettings.getReturnMessage());
        siteSettingsAdminInput.setGuaranteeMessage(currentSettings.getGuaranteeMessage());
        siteSettingsAdminInput.setBrandPaletteJson(jsonContentService.writeJson(currentSettings.getBrandPalette()));
        siteSettingsAdminInput.setTypographyJson(jsonContentService.writeJson(currentSettings.getTypography()));
        siteSettingsAdminInput.setSocialLinksJson(jsonContentService.writeJson(currentSettings.getSocialLinks()));
        siteSettingsAdminInput.setPaymentSettingsJson(jsonContentService.writeJson(currentSettings.getPaymentSettings()));
        siteSettingsAdminInput.setReviewSourceJson(jsonContentService.writeJson(currentSettings.getReviewSource()));
        siteSettingsAdminInput.setFooterContentJson(jsonContentService.writeJson(currentSettings.getFooterContent()));
        siteSettingsAdminInput.setAnnouncementBarsJson(jsonContentService.writeJson(currentSettings.getAnnouncementBars()));
        siteSettingsAdminInput.setPromoBannersJson(jsonContentService.writeJson(currentSettings.getPromoBanners()));
        siteSettingsAdminInput.setTrustBadgesJson(jsonContentService.writeJson(currentSettings.getTrustBadges()));
        siteSettingsService.updateSiteSettings(siteSettingsAdminInput);
        return "redirect:/admin/analytics?saved=1";
    }

    private PaymentSettingsAdminInput toPaymentInput(SiteSettings siteSettings) {
        PaymentSettingsAdminInput paymentSettingsAdminInput = new PaymentSettingsAdminInput();
        java.util.Map<String, Object> paymentSettings = siteSettings.getPaymentSettings();
        paymentSettingsAdminInput.setMode(String.valueOf(paymentSettings.getOrDefault("mode", "sandbox")));
        paymentSettingsAdminInput.setDefaultProvider(String.valueOf(paymentSettings.getOrDefault("defaultProvider", "STRIPE")));
        paymentSettingsAdminInput.setSandboxNotice((String) paymentSettings.getOrDefault(
                "sandboxNotice",
                "Hosted checkout runs in sandbox until production credentials are connected."
        ));
        paymentSettingsAdminInput.setStripeEnabled(Boolean.TRUE.equals(paymentSettings.get("stripeEnabled")));
        paymentSettingsAdminInput.setStripePublishableKey((String) paymentSettings.get("stripePublishableKey"));
        paymentSettingsAdminInput.setStripeAccountId((String) paymentSettings.get("stripeAccountId"));
        paymentSettingsAdminInput.setStripeStatementDescriptor((String) paymentSettings.get("stripeStatementDescriptor"));
        paymentSettingsAdminInput.setPaypalEnabled(Boolean.TRUE.equals(paymentSettings.get("paypalEnabled")));
        paymentSettingsAdminInput.setPaypalClientId((String) paymentSettings.get("paypalClientId"));
        paymentSettingsAdminInput.setPaypalWebhookId((String) paymentSettings.get("paypalWebhookId"));
        paymentSettingsAdminInput.setPaypalMerchantId((String) paymentSettings.get("paypalMerchantId"));
        paymentSettingsAdminInput.setPaypalStatementDescriptor((String) paymentSettings.get("paypalStatementDescriptor"));
        paymentSettingsAdminInput.setManualReviewEnabled(Boolean.TRUE.equals(paymentSettings.getOrDefault("manualReviewEnabled", Boolean.TRUE)));
        paymentSettingsAdminInput.setSaveCardsEnabled(Boolean.TRUE.equals(paymentSettings.getOrDefault("saveCardsEnabled", Boolean.TRUE)));
        return paymentSettingsAdminInput;
    }

    private SiteSettingsAdminInput toInput(SiteSettings siteSettings) {
        SiteSettingsAdminInput siteSettingsAdminInput = new SiteSettingsAdminInput();
        siteSettingsAdminInput.setSiteName(siteSettings.getSiteName());
        siteSettingsAdminInput.setLogoPath(siteSettings.getLogoPath());
        siteSettingsAdminInput.setFaviconPath(siteSettings.getFaviconPath());
        siteSettingsAdminInput.setSupportEmail(siteSettings.getSupportEmail());
        siteSettingsAdminInput.setHeaderCtaText(siteSettings.getHeaderCtaText());
        siteSettingsAdminInput.setShippingMessage(siteSettings.getShippingMessage());
        siteSettingsAdminInput.setReturnMessage(siteSettings.getReturnMessage());
        siteSettingsAdminInput.setGuaranteeMessage(siteSettings.getGuaranteeMessage());
        siteSettingsAdminInput.setBrandPaletteJson(jsonContentService.writeJson(siteSettings.getBrandPalette()));
        siteSettingsAdminInput.setTypographyJson(jsonContentService.writeJson(siteSettings.getTypography()));
        siteSettingsAdminInput.setSocialLinksJson(jsonContentService.writeJson(siteSettings.getSocialLinks()));
        siteSettingsAdminInput.setSeoDefaultsJson(jsonContentService.writeJson(siteSettings.getSeoDefaults()));
        siteSettingsAdminInput.setAnalyticsSettingsJson(jsonContentService.writeJson(siteSettings.getAnalyticsSettings()));
        siteSettingsAdminInput.setPaymentSettingsJson(jsonContentService.writeJson(siteSettings.getPaymentSettings()));
        siteSettingsAdminInput.setReviewSourceJson(jsonContentService.writeJson(siteSettings.getReviewSource()));
        siteSettingsAdminInput.setFooterContentJson(jsonContentService.writeJson(siteSettings.getFooterContent()));
        siteSettingsAdminInput.setAnnouncementBarsJson(jsonContentService.writeJson(siteSettings.getAnnouncementBars()));
        siteSettingsAdminInput.setPromoBannersJson(jsonContentService.writeJson(siteSettings.getPromoBanners()));
        siteSettingsAdminInput.setTrustBadgesJson(jsonContentService.writeJson(siteSettings.getTrustBadges()));
        return siteSettingsAdminInput;
    }
}
