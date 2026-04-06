package org.tavall.platform.auth;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.tavall.platform.core.exception.PlatformDomainException;

@Service
public class PlatformSessionService {

    public static final String SESSION_ATTRIBUTE = "platformSessionUser";

    public void store(HttpSession session, PlatformSessionUser sessionUser) {
        session.setAttribute(SESSION_ATTRIBUTE, sessionUser);
    }

    public Optional<PlatformSessionUser> currentUser(HttpSession session) {
        Object value = session.getAttribute(SESSION_ATTRIBUTE);
        if (value instanceof PlatformSessionUser sessionUser) {
            return Optional.of(sessionUser);
        }
        return Optional.empty();
    }

    public PlatformSessionUser requireUser(HttpSession session) {
        return currentUser(session)
                .orElseThrow(() -> new PlatformDomainException("No platform user was found in the current session."));
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_ATTRIBUTE);
    }
}
