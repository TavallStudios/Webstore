package org.tavall.platform.web.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.tavall.platform.auth.PlatformLoginSuccessHandler;
import org.tavall.platform.auth.PlatformSessionService;
import org.tavall.platform.auth.PlatformUserProvisioningService;

@Configuration
public class PlatformUiSecurityConfiguration {

    @Bean
    @Order(2)
    SecurityFilterChain platformUiSecurityFilterChain(
            HttpSecurity http,
            PlatformUserProvisioningService provisioningService,
            PlatformSessionService platformSessionService,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider
    ) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/login",
                                "/about",
                                "/pricing",
                                "/features",
                                "/platform.css",
                                "/admin.css",
                                "/admin/login",
                                "/auth/**",
                                "/admin/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/app/**", "/onboarding/**", "/admin/**").authenticated()
                        .anyRequest().permitAll()
                );
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .successHandler(new PlatformLoginSuccessHandler(provisioningService, platformSessionService, "/onboarding", "/app/dashboard"))
            );
        }
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );
        return http.build();
    }
}
