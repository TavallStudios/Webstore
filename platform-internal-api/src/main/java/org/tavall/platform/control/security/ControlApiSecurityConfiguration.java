package org.tavall.platform.control.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.tavall.platform.control.config.ControlPlaneProperties;

@Configuration
public class ControlApiSecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain controlApiSecurityFilterChain(HttpSecurity http, ControlPlaneProperties controlPlaneProperties) throws Exception {
        http
                .securityMatcher("/internal/control/**", "/actuator/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new InternalApiKeyAuthenticationFilter(controlPlaneProperties),
                        UsernamePasswordAuthenticationFilter.class
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
