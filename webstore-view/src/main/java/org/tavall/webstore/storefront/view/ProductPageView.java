package org.tavall.webstore.storefront.view;

import java.util.List;
import java.util.Map;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.Promotion;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.model.SiteSettings;

public record ProductPageView(
        SiteSettings siteSettings,
        ContentPage page,
        List<PageSection> sections,
        Product product,
        List<Promotion> promotions,
        Map<String, Boolean> featureFlags
) {
}
