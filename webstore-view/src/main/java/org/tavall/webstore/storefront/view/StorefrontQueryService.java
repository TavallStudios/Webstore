package org.tavall.webstore.storefront.view;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.catalog.model.Promotion;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.PageManagementService;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.Shipment;
import org.tavall.webstore.orders.service.OrderOperationsService;

@Service
public class StorefrontQueryService {

    private final SiteSettingsService siteSettingsService;
    private final FeatureFlagService featureFlagService;
    private final PageManagementService pageManagementService;
    private final CatalogService catalogService;
    private final OrderOperationsService orderOperationsService;

    public StorefrontQueryService(
            SiteSettingsService siteSettingsService,
            FeatureFlagService featureFlagService,
            PageManagementService pageManagementService,
            CatalogService catalogService,
            OrderOperationsService orderOperationsService
    ) {
        this.siteSettingsService = siteSettingsService;
        this.featureFlagService = featureFlagService;
        this.pageManagementService = pageManagementService;
        this.catalogService = catalogService;
        this.orderOperationsService = orderOperationsService;
    }

    @Transactional(readOnly = true)
    public HomePageView buildHomepageView() {
        ContentPage homepage = pageManagementService.getHomepage();
        List<PageSection> sections = pageManagementService.listSections(homepage.getId());
        List<Promotion> promotions = catalogService.listActivePromotions();
        return new HomePageView(
                siteSettingsService.getCurrentSettings(),
                homepage,
                sections,
                catalogService.getFeaturedProduct(),
                promotions,
                featureFlagService.getFeatureFlagMap()
        );
    }

    @Transactional(readOnly = true)
    public ProductPageView buildProductPageView(String slug) {
        ContentPage productPage = pageManagementService.getProductPage();
        return new ProductPageView(
                siteSettingsService.getCurrentSettings(),
                productPage,
                pageManagementService.listSections(productPage.getId()),
                catalogService.getProductForStorefront(slug),
                catalogService.listActivePromotions(),
                featureFlagService.getFeatureFlagMap()
        );
    }

    @Transactional(readOnly = true)
    public TrackingView buildTrackingView(String orderNumber, String email) {
        CustomerOrder customerOrder = orderOperationsService.findTrackableOrder(orderNumber, email);
        Shipment shipment = customerOrder.getShipments().stream().findFirst().orElse(null);
        return new TrackingView(customerOrder, shipment);
    }
}
