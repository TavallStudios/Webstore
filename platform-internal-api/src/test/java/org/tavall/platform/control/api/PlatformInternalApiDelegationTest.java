package org.tavall.platform.control.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.tavall.platform.control.config.ControlPlaneProperties;
import org.tavall.platform.control.security.ControlApiSecurityConfiguration;
import org.tavall.platform.control.service.TenantSiteRuntimeControlService;
import org.tavall.platform.core.command.MutateTenantSiteInfrastructureProfileCommand;
import org.tavall.platform.runtime.TenantRuntimeInfrastructureProfile;
import org.tavall.platform.runtime.TenantSiteDeploymentResult;
import org.tavall.platform.runtime.TenantSiteRuntimePowerState;
import org.tavall.platform.runtime.TenantSiteRuntimeStatus;

@SpringBootTest(
        classes = PlatformInternalApiDelegationTest.TestApplication.class,
        properties = "platform.control.internal-api.shared-secret=test-secret"
)
@AutoConfigureMockMvc
class PlatformInternalApiDelegationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantSiteRuntimeControlService tenantSiteRuntimeControlService;

    @Test
    void launchEndpointRequiresInternalApiKeyAndDelegatesToControlService() throws Exception {
        UUID siteId = UUID.randomUUID();
        UUID requestedByUserId = UUID.randomUUID();
        TenantSiteRuntimeStatus runtimeStatus = new TenantSiteRuntimeStatus(
                TenantSiteRuntimePowerState.PROVISIONING,
                "Provisioning",
                "tenant-site-demo",
                "demo-vm",
                "demo-svc",
                "demo.stores.local",
                "Launch accepted.",
                Instant.now(),
                Map.of("source", "test")
        );
        when(tenantSiteRuntimeControlService.launchTenantSiteRuntime(siteId, requestedByUserId))
                .thenReturn(new TenantSiteDeploymentResult(
                        true,
                        "demo-vm",
                        "Launch accepted.",
                        runtimeStatus,
                        Map.of("jobId", UUID.randomUUID()),
                        Instant.now()
                ));

        mockMvc.perform(post("/internal/control/sites/{siteId}/launch", siteId)
                        .header("X-Platform-Internal-Key", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new RequestedByPayload(requestedByUserId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.deploymentReference").value("demo-vm"))
                .andExpect(jsonPath("$.runtimeStatus.powerState").value("PROVISIONING"));

        verify(tenantSiteRuntimeControlService).launchTenantSiteRuntime(siteId, requestedByUserId);
    }

    @Test
    void statusEndpointRejectsMissingInternalApiKey() throws Exception {
        mockMvc.perform(get("/internal/control/sites/{siteId}/status", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tenantSiteRuntimeControlService);
    }

    @Test
    void infrastructureProfileEndpointDelegatesToControlService() throws Exception {
        UUID siteId = UUID.randomUUID();
        UUID requestedByUserId = UUID.randomUUID();
        when(tenantSiteRuntimeControlService.mutateTenantSiteInfrastructureProfile(
                new MutateTenantSiteInfrastructureProfileCommand(siteId, requestedByUserId, TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT)
        )).thenReturn(new TenantSiteRuntimeStatus(
                TenantSiteRuntimePowerState.FAILED,
                "FAILED",
                "tenant-site-demo",
                "demo-vm",
                "demo-svc",
                "demo.stores.local",
                "Nested KubeVirt target is incompatible on this host.",
                Instant.now(),
                Map.of("compatible", false)
        ));

        mockMvc.perform(put("/internal/control/sites/{siteId}/infrastructure-profile", siteId)
                        .header("X-Platform-Internal-Key", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new MutateTenantSiteInfrastructureProfileCommand(
                                siteId,
                                requestedByUserId,
                                TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("FAILED"))
                .andExpect(jsonPath("$.message").value("Nested KubeVirt target is incompatible on this host."));

        verify(tenantSiteRuntimeControlService).mutateTenantSiteInfrastructureProfile(
                new MutateTenantSiteInfrastructureProfileCommand(siteId, requestedByUserId, TenantRuntimeInfrastructureProfile.NESTED_KUBEVIRT)
        );
    }

    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            FlywayAutoConfiguration.class
    })
    @EnableConfigurationProperties(ControlPlaneProperties.class)
    @Import({PlatformControlApiController.class, ControlApiSecurityConfiguration.class})
    static class TestApplication {
    }
}
