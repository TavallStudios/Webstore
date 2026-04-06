package org.tavall.webstore.content.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.content.model.FeatureFlag;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    List<FeatureFlag> findAllByOrderByModuleNameAscDisplayNameAsc();

    Optional<FeatureFlag> findByFlagKey(String flagKey);
}
