package org.tavall.platform.admin.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.tavall.platform.auth.PlatformSessionService;

@ControllerAdvice
public class PlatformAdminModelAdvice {

    private final PlatformSessionService platformSessionService;

    public PlatformAdminModelAdvice(PlatformSessionService platformSessionService) {
        this.platformSessionService = platformSessionService;
    }

    @ModelAttribute("currentUser")
    public Object currentUser(HttpSession session) {
        return platformSessionService.currentUser(session).orElse(null);
    }
}
