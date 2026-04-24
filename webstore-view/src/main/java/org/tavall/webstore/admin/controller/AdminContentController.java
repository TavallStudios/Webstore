package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.admin.input.PageSectionAdminInput;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.service.JsonContentService;
import org.tavall.webstore.content.service.PageManagementService;

@Controller
public class AdminContentController {

    private final PageManagementService pageManagementService;
    private final CatalogService catalogService;
    private final JsonContentService jsonContentService;

    public AdminContentController(
            PageManagementService pageManagementService,
            CatalogService catalogService,
            JsonContentService jsonContentService
    ) {
        this.pageManagementService = pageManagementService;
        this.catalogService = catalogService;
        this.jsonContentService = jsonContentService;
    }

    @GetMapping("/admin/pages/{slug}")
    public String pageEditor(@PathVariable String slug, @RequestParam(required = false) Long sectionId, Model model) {
        ContentPage contentPage = pageManagementService.getPageBySlug(slug);
        java.util.List<PageSection> sections = pageManagementService.listSections(contentPage.getId());
        PageSection selectedSection = sections.stream()
                .filter(section -> sectionId != null && section.getId().equals(sectionId))
                .findFirst()
                .orElse(sections.stream().findFirst().orElse(null));
        PageSectionAdminInput selectedSectionInput = selectedSection == null ? new PageSectionAdminInput() : toSectionInput(selectedSection);
        selectedSectionInput.setContentPageId(contentPage.getId());

        model.addAttribute("page", contentPage);
        model.addAttribute("sections", sections);
        model.addAttribute("selectedSection", selectedSectionInput);
        model.addAttribute("previewPath", "home".equals(slug) ? "/" : "/products/" + catalogService.getFeaturedProduct().getSlug());
        return "admin/page-editor";
    }

    @PostMapping("/admin/pages/sections")
    public String saveSection(@ModelAttribute PageSectionAdminInput pageSectionAdminInput) {
        PageSection pageSection = pageManagementService.saveSection(pageSectionAdminInput);
        return "redirect:/admin/pages/" + pageSection.getContentPage().getSlug() + "?sectionId=" + pageSection.getId();
    }

    @PostMapping("/admin/pages/sections/{sectionId}/move-up")
    public String moveSectionUp(@PathVariable Long sectionId, @RequestParam String pageSlug) {
        pageManagementService.moveSection(sectionId, -1);
        return "redirect:/admin/pages/" + pageSlug + "?sectionId=" + sectionId;
    }

    @PostMapping("/admin/pages/sections/{sectionId}/move-down")
    public String moveSectionDown(@PathVariable Long sectionId, @RequestParam String pageSlug) {
        pageManagementService.moveSection(sectionId, 1);
        return "redirect:/admin/pages/" + pageSlug + "?sectionId=" + sectionId;
    }

    private PageSectionAdminInput toSectionInput(PageSection pageSection) {
        PageSectionAdminInput pageSectionAdminInput = new PageSectionAdminInput();
        pageSectionAdminInput.setId(pageSection.getId());
        pageSectionAdminInput.setContentPageId(pageSection.getContentPage().getId());
        pageSectionAdminInput.setSectionKey(pageSection.getSectionKey());
        pageSectionAdminInput.setDisplayName(pageSection.getDisplayName());
        pageSectionAdminInput.setSectionType(pageSection.getSectionType());
        pageSectionAdminInput.setPlacement(pageSection.getPlacement());
        pageSectionAdminInput.setActive(pageSection.isActive());
        pageSectionAdminInput.setPosition(pageSection.getPosition());
        pageSectionAdminInput.setMobilePosition(pageSection.getMobilePosition());
        pageSectionAdminInput.setConfigurationJson(jsonContentService.writeJson(pageSection.getConfiguration()));
        return pageSectionAdminInput;
    }
}
