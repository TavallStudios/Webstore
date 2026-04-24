package org.tavall.webstore.content.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageType;

public interface ContentPageRepository extends JpaRepository<ContentPage, Long> {

    Optional<ContentPage> findBySlug(String slug);

    Optional<ContentPage> findByPageTypeAndActiveTrue(PageType pageType);
}
