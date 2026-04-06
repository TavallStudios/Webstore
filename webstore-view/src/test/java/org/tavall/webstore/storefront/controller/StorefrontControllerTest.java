package org.tavall.webstore.storefront.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.cart.service.CartSessionService;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.SubscriptionPlan;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.model.PageType;
import org.tavall.webstore.content.model.SiteSettings;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.service.OrderOperationsService;
import org.tavall.webstore.storefront.view.HomePageView;
import org.tavall.webstore.storefront.view.ProductPageView;
import org.tavall.webstore.storefront.view.StorefrontQueryService;
import org.tavall.webstore.theme.service.ThemeAssetService;
import org.tavall.webstore.theme.view.ThemeRenderView;

@WebMvcTest(StorefrontController.class)
class StorefrontControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorefrontQueryService storefrontQueryService;

    @MockBean
    private OrderOperationsService orderOperationsService;

    @MockBean
    private SiteSettingsService siteSettingsService;

    @MockBean
    private FeatureFlagService featureFlagService;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartSessionService cartSessionService;

    @MockBean
    private ThemeAssetService themeAssetService;

    private SiteSettings siteSettings;

    @BeforeEach
    void setUp() {
        siteSettings = new SiteSettings();
        siteSettings.setSiteName("Atlas Bottle");
        siteSettings.setSupportEmail("support@example.test");
        siteSettings.setBrandPalette(Map.of(
                "background", "#f7efe4",
                "surface", "#fffaf3",
                "ink", "#1d1a16",
                "accent", "#c85f35",
                "accentDeep", "#8c3514",
                "muted", "#6d6258"
        ));
        siteSettings.setTypography(Map.of(
                "headline", "Fraunces, Georgia, serif",
                "body", "IBM Plex Sans, sans-serif",
                "mono", "IBM Plex Mono, monospace"
        ));
        siteSettings.setAnnouncementBars(List.of(Map.of("message", "Launch week")));
        siteSettings.setTrustBadges(List.of(Map.of("label", "Free shipping", "detail", "Over $50")));
        siteSettings.setFooterContent(Map.of("finePrint", "Demo"));
        when(siteSettingsService.getCurrentSettings()).thenReturn(siteSettings);
        when(featureFlagService.getFeatureFlagMap()).thenReturn(Map.of());
        when(cartService.getCartItemCount(any())).thenReturn(0);
        when(themeAssetService.buildRenderView()).thenReturn(ThemeRenderView.empty());
    }

    @Test
    void homepageRendersHomeTemplate() throws Exception {
        Product product = new Product();
        product.setSlug("atlas-bottle");
        product.setName("Atlas Bottle");
        product.setPrice(BigDecimal.valueOf(42));

        ContentPage contentPage = new ContentPage();
        contentPage.setTitle("Home");
        contentPage.setPageType(PageType.HOMEPAGE);
        contentPage.setSeoMetadata(Map.of("title", "Atlas Home"));

        when(storefrontQueryService.buildHomepageView()).thenReturn(
                new HomePageView(siteSettings, contentPage, List.of(), product, List.of(), Map.of())
        );

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/home"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void orderConfirmationRendersConfirmationTemplate() throws Exception {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100001");
        customerOrder.setCustomerName("Morgan Lee");
        customerOrder.setEmail("customer@example.com");
        customerOrder.setTotalAmount(BigDecimal.valueOf(75.60));
        customerOrder.setLineItems(List.of());
        when(orderOperationsService.getDetailedOrder("WS-100001")).thenReturn(customerOrder);

        mockMvc.perform(get("/orders/WS-100001/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/order-confirmation"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    void productPageRendersProductTemplate() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setSlug("atlas-bottle");
        product.setName("Atlas Bottle");
        product.setTagline("Leak-proof insulated bottle.");
        product.setShortDescription("Built for clean commutes.");
        product.setPrice(BigDecimal.valueOf(42));
        product.setCompareAtPrice(BigDecimal.valueOf(52));
        product.setMediaAssets(List.of(Map.of("path", "/media/atlas-bottle-hero.svg", "alt", "Atlas Bottle hero")));
        product.setBadges(List.of("Best Seller"));
        product.setHighlights(List.of("Leak-proof lock lid"));
        product.setFaqEntries(List.of(Map.of("question", "Is it dishwasher safe?", "answer", "Yes.")));
        product.setGuaranteeText("30-day guarantee");
        product.setReviewSummary(Map.of("rating", "4.9"));
        product.setPresentationMetadata(Map.of("showQuantitySelector", true, "stickyAddToCart", true));

        BundleOffer bundleOffer = new BundleOffer();
        bundleOffer.setId(1L);
        bundleOffer.setName("Twin Pack");
        bundleOffer.setDescription("Two bottles for the desk-and-gym setup.");
        bundleOffer.setBundleQuantity(2);
        product.setBundleOffers(List.of(bundleOffer));

        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(1L);
        subscriptionPlan.setName("Monthly refresh");
        subscriptionPlan.setIntervalLabel("Every 30 days");
        subscriptionPlan.setDiscountPercent(8);
        product.setSubscriptionPlans(List.of(subscriptionPlan));

        ContentPage contentPage = new ContentPage();
        contentPage.setTitle("Product");
        contentPage.setPageType(PageType.PRODUCT);

        PageSection specsSection = new PageSection();
        specsSection.setSectionType("specs");
        specsSection.setDisplayName("Specs");
        specsSection.setConfiguration(Map.of(
                "title", "Materials and daily details",
                "items", List.of(Map.of("label", "Capacity", "value", "20 oz"))
        ));

        when(featureFlagService.getFeatureFlagMap()).thenReturn(Map.of(
                "bundles_enabled", true,
                "subscriptions_enabled", true
        ));
        when(storefrontQueryService.buildProductPageView("atlas-bottle")).thenReturn(
                new ProductPageView(siteSettings, contentPage, List.of(specsSection), product, List.of(), Map.of())
        );

        mockMvc.perform(get("/products/atlas-bottle"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/product"))
                .andExpect(model().attributeExists("view"));
    }

    @Test
    void trackingLookupPostsToTrackingDetailTemplate() throws Exception {
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100001");
        when(storefrontQueryService.buildTrackingView("WS-100001", "customer@example.com"))
                .thenReturn(new org.tavall.webstore.storefront.view.TrackingView(customerOrder, null));

        mockMvc.perform(post("/tracking")
                        .param("orderNumber", "WS-100001")
                        .param("email", "customer@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("storefront/tracking-detail"));
    }
}
