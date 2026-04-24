package org.tavall.platform.admin.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.tavall.platform.auth.PlatformSessionService;
import org.tavall.platform.auth.PlatformSessionUser;
import org.tavall.platform.core.PlatformRole;

@Service
public class PlatformAdminAccessService {

    private final PlatformSessionService platformSessionService;

    public PlatformAdminAccessService(PlatformSessionService platformSessionService) {
        this.platformSessionService = platformSessionService;
    }

    public PlatformSessionUser requireMasterAdmin(HttpSession session) {
        PlatformSessionUser sessionUser = platformSessionService.requireUser(session);
        if (!sessionUser.hasRole(PlatformRole.MASTER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Master admin access is required.");
        }
        return sessionUser;
    }
}
