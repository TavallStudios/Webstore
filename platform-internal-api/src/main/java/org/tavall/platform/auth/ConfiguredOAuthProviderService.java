package org.tavall.platform.auth;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguredOAuthProviderService {

    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    public ConfiguredOAuthProviderService(ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) {
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
    }

    public List<ConfiguredOAuthProviderView> listConfiguredProviders() {
        List<ConfiguredOAuthProviderView> providers = new ArrayList<>();
        ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepositoryProvider.getIfAvailable();
        if (clientRegistrationRepository == null) {
            return providers;
        }
        if (clientRegistrationRepository instanceof Iterable<?> iterable) {
            for (Object registrationObject : iterable) {
                ClientRegistration registration = (ClientRegistration) registrationObject;
                providers.add(new ConfiguredOAuthProviderView(
                        registration.getRegistrationId(),
                        registration.getClientName(),
                        "/oauth2/authorization/" + registration.getRegistrationId()
                ));
            }
        }
        return providers;
    }
}
