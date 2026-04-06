package org.tavall.platform.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.tavall.platform.core.PlatformRole;

public class PlatformLoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String POST_LOGIN_TARGET_ATTRIBUTE = "platform.auth.postLoginTarget";

    private final PlatformUserProvisioningService provisioningService;
    private final PlatformSessionService sessionService;
    private final String onboardingTargetUrl;
    private final String defaultTargetUrl;

    public PlatformLoginSuccessHandler(
            PlatformUserProvisioningService provisioningService,
            PlatformSessionService sessionService,
            String onboardingTargetUrl,
            String defaultTargetUrl
    ) {
        this.provisioningService = provisioningService;
        this.sessionService = sessionService;
        this.onboardingTargetUrl = onboardingTargetUrl;
        this.defaultTargetUrl = defaultTargetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String registrationId = request.getRequestURI().contains("/oauth2/authorization/")
                ? request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1)
                : ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication)
                .getAuthorizedClientRegistrationId();
        ProvisionedPlatformSession provisionedSession = provisioningService.provisionFromSocialLogin(
                registrationId,
                principal.getAttributes()
        );
        sessionService.store(request.getSession(true), provisionedSession.sessionUser());
        String requestedTargetUrl = (String) request.getSession().getAttribute(POST_LOGIN_TARGET_ATTRIBUTE);
        request.getSession().removeAttribute(POST_LOGIN_TARGET_ATTRIBUTE);
        String targetUrl;
        if (provisionedSession.onboardingRequired()) {
            targetUrl = onboardingTargetUrl;
        } else if (requestedTargetUrl != null
                && !requestedTargetUrl.isBlank()
                && ("/admin".equals(requestedTargetUrl)
                ? provisionedSession.sessionUser().hasRole(PlatformRole.MASTER_ADMIN)
                : true)) {
            targetUrl = requestedTargetUrl;
        } else {
            targetUrl = defaultTargetUrl;
        }
        response.sendRedirect(targetUrl);
    }
}
