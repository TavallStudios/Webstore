package org.tavall.platform.auth;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.platform.core.PlatformRole;
import org.tavall.platform.core.TenantMembershipRole;
import org.tavall.platform.core.TenantReadinessState;
import org.tavall.platform.core.util.Slugifier;
import org.tavall.platform.persistence.entity.PlatformProviderIdentity;
import org.tavall.platform.persistence.entity.PlatformUser;
import org.tavall.platform.persistence.entity.TenantAccount;
import org.tavall.platform.persistence.entity.TenantMembership;
import org.tavall.platform.persistence.entity.TenantOnboardingState;
import org.tavall.platform.persistence.repository.PlatformProviderIdentityRepository;
import org.tavall.platform.persistence.repository.PlatformUserRepository;
import org.tavall.platform.persistence.repository.TenantAccountRepository;
import org.tavall.platform.persistence.repository.TenantMembershipRepository;
import org.tavall.platform.persistence.repository.TenantOnboardingStateRepository;

@Service
public class PlatformUserProvisioningService {

    private final PlatformUserRepository platformUserRepository;
    private final PlatformProviderIdentityRepository providerIdentityRepository;
    private final TenantAccountRepository tenantAccountRepository;
    private final TenantMembershipRepository tenantMembershipRepository;
    private final TenantOnboardingStateRepository tenantOnboardingStateRepository;
    private final PlatformOAuthProfileExtractor profileExtractor;
    private final PlatformAuthProperties authProperties;
    private final Slugifier slugifier;

    public PlatformUserProvisioningService(
            PlatformUserRepository platformUserRepository,
            PlatformProviderIdentityRepository providerIdentityRepository,
            TenantAccountRepository tenantAccountRepository,
            TenantMembershipRepository tenantMembershipRepository,
            TenantOnboardingStateRepository tenantOnboardingStateRepository,
            PlatformOAuthProfileExtractor profileExtractor,
            PlatformAuthProperties authProperties,
            Slugifier slugifier
    ) {
        this.platformUserRepository = platformUserRepository;
        this.providerIdentityRepository = providerIdentityRepository;
        this.tenantAccountRepository = tenantAccountRepository;
        this.tenantMembershipRepository = tenantMembershipRepository;
        this.tenantOnboardingStateRepository = tenantOnboardingStateRepository;
        this.profileExtractor = profileExtractor;
        this.authProperties = authProperties;
        this.slugifier = slugifier;
    }

    @Transactional
    public ProvisionedPlatformSession provisionFromSocialLogin(String registrationId, Map<String, Object> attributes) {
        AuthProviderProfile profile = profileExtractor.extract(registrationId, attributes);
        PlatformProviderIdentity identity = providerIdentityRepository
                .findByProviderTypeAndProviderSubject(profile.providerType(), profile.providerSubject())
                .orElseGet(() -> createOrLinkIdentity(profile));

        PlatformUser platformUser = identity.getPlatformUser();
        refreshUser(profile, platformUser);
        refreshIdentity(profile, identity);

        platformUserRepository.save(platformUser);
        providerIdentityRepository.save(identity);

        var memberships = tenantMembershipRepository.findByPlatformUserIdOrderByCreatedAtAsc(platformUser.getId());
        TenantAccount tenantAccount = memberships.isEmpty() ? null : memberships.getFirst().getTenantAccount();
        TenantOnboardingState onboardingState = tenantAccount == null
                ? null
                : tenantOnboardingStateRepository.findByTenantAccountId(tenantAccount.getId()).orElse(null);

        PlatformSessionUser sessionUser = new PlatformSessionUser(
                platformUser.getId(),
                tenantAccount == null ? null : tenantAccount.getId(),
                platformUser.getEmail(),
                platformUser.getDisplayName(),
                platformUser.getAvatarUrl(),
                Set.copyOf(platformUser.getRoles())
        );
        boolean onboardingRequired = onboardingState == null || onboardingState.getCompletedAt() == null;
        return new ProvisionedPlatformSession(sessionUser, onboardingRequired);
    }

    private PlatformProviderIdentity createOrLinkIdentity(AuthProviderProfile profile) {
        PlatformUser platformUser = platformUserRepository.findByEmailIgnoreCase(profile.email())
                .orElseGet(() -> createPlatformUser(profile));

        PlatformProviderIdentity identity = new PlatformProviderIdentity();
        identity.setPlatformUser(platformUser);
        refreshIdentity(profile, identity);
        providerIdentityRepository.save(identity);
        return identity;
    }

    private PlatformUser createPlatformUser(AuthProviderProfile profile) {
        PlatformUser user = new PlatformUser();
        user.setDisplayName(profile.displayName());
        user.setEmail(profile.email());
        user.setAvatarUrl(profile.avatarUrl());
        user.setLastLoginAt(Instant.now());
        Set<PlatformRole> roles = new LinkedHashSet<>();
        roles.add(PlatformRole.TENANT_USER);
        roles.add(PlatformRole.TENANT_ADMIN);
        if (profile.email() != null && authProperties.getMasterAdminEmails().stream().anyMatch(profile.email()::equalsIgnoreCase)) {
            roles.add(PlatformRole.MASTER_ADMIN);
        }
        user.setRoles(roles);
        PlatformUser savedUser = platformUserRepository.save(user);

        TenantAccount tenantAccount = new TenantAccount();
        tenantAccount.setDisplayName(profile.displayName() + " Workspace");
        tenantAccount.setSlug(allocateTenantSlug(profile.displayName()));
        tenantAccount.setSettings(Map.of("createdVia", "social-login"));
        tenantAccountRepository.save(tenantAccount);

        TenantMembership membership = new TenantMembership();
        membership.setPlatformUser(savedUser);
        membership.setTenantAccount(tenantAccount);
        membership.setRole(TenantMembershipRole.OWNER);
        tenantMembershipRepository.save(membership);

        TenantOnboardingState onboardingState = new TenantOnboardingState();
        onboardingState.setTenantAccount(tenantAccount);
        onboardingState.setReadinessState(TenantReadinessState.PROFILE_PENDING);
        onboardingState.setCurrentStep("workspace");
        onboardingState.setDraftData(Map.of("provider", profile.providerType().name()));
        tenantOnboardingStateRepository.save(onboardingState);

        return savedUser;
    }

    private void refreshUser(AuthProviderProfile profile, PlatformUser platformUser) {
        if (profile.email() != null && !profile.email().isBlank()) {
            platformUser.setEmail(profile.email());
        }
        platformUser.setDisplayName(profile.displayName());
        platformUser.setAvatarUrl(profile.avatarUrl());
        platformUser.setLastLoginAt(Instant.now());
        if (profile.email() != null && authProperties.getMasterAdminEmails().stream().anyMatch(profile.email()::equalsIgnoreCase)) {
            platformUser.getRoles().add(PlatformRole.MASTER_ADMIN);
        }
    }

    private void refreshIdentity(AuthProviderProfile profile, PlatformProviderIdentity identity) {
        identity.setProviderType(profile.providerType());
        identity.setProviderSubject(profile.providerSubject());
        identity.setEmailSnapshot(profile.email());
        identity.setDisplayNameSnapshot(profile.displayName());
        identity.setAvatarUrlSnapshot(profile.avatarUrl());
        identity.setProviderMetadata(profile.providerMetadata());
        identity.setLastLoginAt(Instant.now());
    }

    private String allocateTenantSlug(String displayName) {
        String baseSlug = slugifier.toSlug(displayName);
        String candidate = baseSlug;
        int counter = 1;
        while (tenantAccountRepository.findBySlug(candidate).isPresent()) {
            counter++;
            candidate = baseSlug + "-" + counter;
        }
        return candidate;
    }
}
