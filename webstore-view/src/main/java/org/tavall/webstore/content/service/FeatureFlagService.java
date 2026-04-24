package org.tavall.webstore.content.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.content.model.FeatureFlag;
import org.tavall.webstore.content.repository.FeatureFlagRepository;

@Service
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    public FeatureFlagService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> listFeatureFlags() {
        return featureFlagRepository.findAllByOrderByModuleNameAscDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> getFeatureFlagMap() {
        Map<String, Boolean> featureFlagMap = new LinkedHashMap<>();
        for (FeatureFlag featureFlag : featureFlagRepository.findAllByOrderByModuleNameAscDisplayNameAsc()) {
            featureFlagMap.put(featureFlag.getFlagKey(), featureFlag.isEnabled());
        }
        return featureFlagMap;
    }

    @Transactional
    public void updateFeatureFlag(String flagKey, boolean enabled) {
        FeatureFlag featureFlag = featureFlagRepository.findByFlagKey(flagKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown feature flag: " + flagKey));
        featureFlag.setEnabled(enabled);
        featureFlagRepository.save(featureFlag);
    }
}
