package org.tavall.platform.control.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tavall.platform.control.config.ControlPlaneProperties;

public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ControlPlaneProperties controlPlaneProperties;

    public InternalApiKeyAuthenticationFilter(ControlPlaneProperties controlPlaneProperties) {
        this.controlPlaneProperties = controlPlaneProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String providedSecret = request.getHeader("X-Platform-Internal-Key");
        if (!controlPlaneProperties.getInternalApi().getSharedSecret().equals(providedSecret)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid internal platform API key.");
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                "platform-internal-api",
                providedSecret,
                AuthorityUtils.createAuthorityList("ROLE_PLATFORM_INTERNAL_API")
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
