package org.tavall.webstore.content.service;

import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.admin.input.PageSectionAdminInput;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.model.PageType;
import org.tavall.webstore.content.repository.ContentPageRepository;
import org.tavall.webstore.content.repository.PageSectionRepository;

@Service
public class PageManagementService {

    private final ContentPageRepository contentPageRepository;
    private final PageSectionRepository pageSectionRepository;
    private final JsonContentService jsonContentService;

    public PageManagementService(
            ContentPageRepository contentPageRepository,
            PageSectionRepository pageSectionRepository,
            JsonContentService jsonContentService
    ) {
        this.contentPageRepository = contentPageRepository;
        this.pageSectionRepository = pageSectionRepository;
        this.jsonContentService = jsonContentService;
    }

    @Transactional(readOnly = true)
    public ContentPage getHomepage() {
        return contentPageRepository.findByPageTypeAndActiveTrue(PageType.HOMEPAGE)
                .orElseThrow(() -> new IllegalStateException("Homepage configuration is missing."));
    }

    @Transactional(readOnly = true)
    public ContentPage getProductPage() {
        return contentPageRepository.findByPageTypeAndActiveTrue(PageType.PRODUCT)
                .orElseThrow(() -> new IllegalStateException("Product page configuration is missing."));
    }

    @Transactional(readOnly = true)
    public ContentPage getPageBySlug(String slug) {
        return contentPageRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Unknown page slug: " + slug));
    }

    @Transactional(readOnly = true)
    public List<PageSection> listSections(Long contentPageId) {
        return pageSectionRepository.findAllByContentPageIdOrderByPositionAscIdAsc(contentPageId);
    }

    @Transactional
    public PageSection saveSection(PageSectionAdminInput input) {
        PageSection pageSection = input.getId() == null
                ? new PageSection()
                : pageSectionRepository.findById(input.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown page section."));
        ContentPage contentPage = contentPageRepository.findById(input.getContentPageId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown page."));
        pageSection.setContentPage(contentPage);
        pageSection.setSectionKey(input.getSectionKey());
        pageSection.setDisplayName(input.getDisplayName());
        pageSection.setSectionType(input.getSectionType());
        pageSection.setPlacement(input.getPlacement());
        pageSection.setActive(input.isActive());
        pageSection.setPosition(input.getPosition());
        pageSection.setMobilePosition(input.getMobilePosition());
        pageSection.setConfiguration(new HashMap<>(jsonContentService.parseObject(input.getConfigurationJson())));
        return pageSectionRepository.save(pageSection);
    }

    @Transactional
    public void moveSection(Long sectionId, int direction) {
        PageSection selectedSection = pageSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown page section."));
        List<PageSection> sections = pageSectionRepository.findAllByContentPageIdOrderByPositionAscIdAsc(
                selectedSection.getContentPage().getId());
        int currentIndex = -1;
        for (int index = 0; index < sections.size(); index++) {
            if (sections.get(index).getId().equals(selectedSection.getId())) {
                currentIndex = index;
                break;
            }
        }
        int targetIndex = currentIndex + direction;
        if (currentIndex < 0 || targetIndex < 0 || targetIndex >= sections.size()) {
            return;
        }

        PageSection swapSection = sections.get(targetIndex);
        int originalPosition = selectedSection.getPosition();
        selectedSection.setPosition(swapSection.getPosition());
        swapSection.setPosition(originalPosition);
        pageSectionRepository.save(selectedSection);
        pageSectionRepository.save(swapSection);
    }
}
