package org.tavall.webstore.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.content.model.SiteSettings;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Long> {
}
