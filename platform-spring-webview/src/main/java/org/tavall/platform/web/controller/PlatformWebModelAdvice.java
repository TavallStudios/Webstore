package org.tavall.platform.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.tavall.platform.auth.PlatformSessionService;

@ControllerAdvice
public class PlatformWebModelAdvice {

    private final PlatformSessionService platformSessionService;

    public PlatformWebModelAdvice(PlatformSessionService platformSessionService) {
        this.platformSessionService = platformSessionService;
    }

    @ModelAttribute("currentUser")
    public Object currentUser(HttpSession session) {
        return platformSessionService.currentUser(session).orElse(null);
    }
}
