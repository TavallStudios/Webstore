package org.tavall.webstore.content.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tavall.webstore.content.model.PageSection;

public interface PageSectionRepository extends JpaRepository<PageSection, Long> {

    List<PageSection> findAllByContentPageIdOrderByPositionAscIdAsc(Long contentPageId);
}
