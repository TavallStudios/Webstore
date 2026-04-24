package org.tavall.platform.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tavall.platform.auth.PlatformLoginSuccessHandler;

@Controller
public class PlatformAuthenticationEntryController {

    @GetMapping("/auth/{registrationId}")
    public String startPlatformLogin(@PathVariable String registrationId, HttpSession session) {
        session.setAttribute(PlatformLoginSuccessHandler.POST_LOGIN_TARGET_ATTRIBUTE, "/app/dashboard");
        return "redirect:/oauth2/authorization/" + registrationId;
    }

    @GetMapping("/admin/auth/{registrationId}")
    public String startAdminLogin(@PathVariable String registrationId, HttpSession session) {
        session.setAttribute(PlatformLoginSuccessHandler.POST_LOGIN_TARGET_ATTRIBUTE, "/admin");
        return "redirect:/oauth2/authorization/" + registrationId;
    }
}
