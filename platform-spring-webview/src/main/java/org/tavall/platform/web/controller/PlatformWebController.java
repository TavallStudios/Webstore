package org.tavall.platform.web.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.tavall.platform.auth.ConfiguredOAuthProviderService;
import org.tavall.platform.auth.PlatformSessionService;
import org.tavall.platform.persistence.repository.TenantOnboardingStateRepository;
import org.tavall.platform.persistence.repository.TenantSiteRepository;
import org.tavall.platform.web.model.PlatformOnboardingForm;
import org.tavall.platform.web.service.PlatformTenantOnboardingService;

@Controller
public class PlatformWebController {

    private final ConfiguredOAuthProviderService configuredOAuthProviderService;
    private final PlatformSessionService platformSessionService;
    private final TenantOnboardingStateRepository onboardingStateRepository;
    private final TenantSiteRepository tenantSiteRepository;
    private final PlatformTenantOnboardingService platformTenantOnboardingService;

    public PlatformWebController(
            ConfiguredOAuthProviderService configuredOAuthProviderService,
            PlatformSessionService platformSessionService,
            TenantOnboardingStateRepository onboardingStateRepository,
            TenantSiteRepository tenantSiteRepository,
            PlatformTenantOnboardingService platformTenantOnboardingService
    ) {
        this.configuredOAuthProviderService = configuredOAuthProviderService;
        this.platformSessionService = platformSessionService;
        this.onboardingStateRepository = onboardingStateRepository;
        this.tenantSiteRepository = tenantSiteRepository;
        this.platformTenantOnboardingService = platformTenantOnboardingService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("providers", configuredOAuthProviderService.listConfiguredProviders());
        return "platform/home";
    }

    @GetMapping("/features")
    public String features() {
        return "platform/features";
    }

    @GetMapping("/pricing")
    public String pricing() {
        return "platform/pricing";
    }

    @GetMapping("/about")
    public String about() {
        return "platform/about";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("providers", configuredOAuthProviderService.listConfiguredProviders());
        return "platform/login";
    }

    @GetMapping("/app")
    public String app(HttpSession session) {
        var sessionUser = platformSessionService.requireUser(session);
        var onboarding = onboardingStateRepository.findByTenantAccountId(sessionUser.activeTenantAccountId()).orElseThrow();
        return onboarding.getCompletedAt() == null ? "redirect:/onboarding" : "redirect:/app/dashboard";
    }

    @GetMapping("/onboarding")
    public String onboarding(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PlatformOnboardingForm());
        }
        return "platform/onboarding";
    }

    @PostMapping("/onboarding")
    public String completeOnboarding(
            @Valid @ModelAttribute("form") PlatformOnboardingForm form,
            BindingResult bindingResult,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return "platform/onboarding";
        }
        var sessionUser = platformSessionService.requireUser(session);
        platformTenantOnboardingService.completeOnboarding(sessionUser, form);
        return "redirect:/app/dashboard";
    }

    @GetMapping("/app/dashboard")
    public String dashboard(Model model, HttpSession session) {
        var sessionUser = platformSessionService.requireUser(session);
        model.addAttribute("sites", tenantSiteRepository.findByTenantAccountIdOrderByCreatedAtDesc(sessionUser.activeTenantAccountId()));
        model.addAttribute("onboarding", onboardingStateRepository.findByTenantAccountId(sessionUser.activeTenantAccountId()).orElse(null));
        return "platform/dashboard";
    }
}
