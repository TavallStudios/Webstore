package org.tavall.webstore.admin.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.cart.service.CartSessionService;
import org.tavall.webstore.config.StorefrontProperties;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.SubscriptionPlan;
import org.tavall.webstore.catalog.repository.ProductRepository;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.FeatureFlag;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.model.PageType;
import org.tavall.webstore.content.model.SiteSettings;
import org.tavall.webstore.content.repository.ContentPageRepository;
import org.tavall.webstore.content.repository.PageSectionRepository;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.JsonContentService;
import org.tavall.webstore.content.service.PageManagementService;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.media.service.MediaAssetService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.FulfillmentStatus;
import org.tavall.webstore.orders.model.OrderLineItem;
import org.tavall.webstore.orders.model.PaymentStatus;
import org.tavall.webstore.orders.service.OrderOperationsService;
import org.tavall.webstore.storefront.view.MediaAssetDescriptor;
import org.tavall.webstore.theme.service.ThemeAssetService;
import org.tavall.webstore.theme.view.ThemeFileView;
import org.tavall.webstore.theme.view.ThemeRenderView;

@WebMvcTest({
        AdminDashboardController.class,
        AdminConfigurationController.class,
        AdminOperationsController.class,
        AdminCatalogController.class,
        AdminContentController.class,
        AdminMediaController.class,
        AdminThemeController.class
})
class AdminControllerRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private ContentPageRepository contentPageRepository;

    @MockBean
    private PageSectionRepository pageSectionRepository;

    @MockBean
    private FeatureFlagService featureFlagService;

    @MockBean
    private SiteSettingsService siteSettingsService;

    @MockBean
    private OrderOperationsService orderOperationsService;

    @MockBean
    private MediaAssetService mediaAssetService;

    @MockBean
    private ThemeAssetService themeAssetService;

    @MockBean
    private StorefrontProperties storefrontProperties;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private JsonContentService jsonContentService;

    @MockBean
    private PageManagementService pageManagementService;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartSessionService cartSessionService;

    @BeforeEach
    void setUp() {
        StorefrontProperties.Assets assets = new StorefrontProperties.Assets();
        assets.setDevServerUrl("");

        SiteSettings siteSettings = new SiteSettings();
        siteSettings.setSiteName("Atlas Bottle");
        siteSettings.setSupportEmail("support@example.test");
        siteSettings.setLogoPath("/media/atlas-wordmark.svg");
        siteSettings.setFaviconPath("/media/atlas-wordmark.svg");
        siteSettings.setHeaderCtaText("Shop now");
        siteSettings.setShippingMessage("Ships in 2 business days.");
        siteSettings.setReturnMessage("30-day returns.");
        siteSettings.setGuaranteeMessage("Backed by a lifetime guarantee.");
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
                "body", "IBM Plex Sans, Segoe UI, sans-serif",
                "mono", "IBM Plex Mono, Consolas, monospace"
        ));
        siteSettings.setSocialLinks(Map.of("instagram", "https://example.test/atlas"));
        siteSettings.setSeoDefaults(Map.of("title", "Atlas Bottle"));
        siteSettings.setAnalyticsSettings(Map.of("ga4MeasurementId", "G-ATLAS"));
        siteSettings.setReviewSource(Map.of("label", "4.9 / 5 average", "source", "verified buyers"));
        siteSettings.setFooterContent(Map.of(
                "finePrint", "Built for daily carry.",
                "columns", List.of(Map.of(
                        "title", "Support",
                        "links", List.of(Map.of("label", "Track order", "href", "/tracking"))
                ))
        ));
        siteSettings.setAnnouncementBars(List.of(Map.of("message", "Launch week")));
        siteSettings.setPromoBanners(List.of(Map.of("headline", "Copper Clay ships this week")));
        siteSettings.setTrustBadges(List.of(Map.of("label", "Free shipping", "detail", "Orders over $50")));

        Map<String, Object> paymentSettings = new HashMap<>();
        paymentSettings.put("mode", "sandbox");
        paymentSettings.put("defaultProvider", "STRIPE");
        paymentSettings.put("sandboxNotice", "Hosted checkout runs in sandbox until production credentials are connected.");
        paymentSettings.put("stripeEnabled", Boolean.FALSE);
        paymentSettings.put("stripePublishableKey", "");
        paymentSettings.put("stripeSecretKey", "");
        paymentSettings.put("stripeWebhookSecret", "");
        paymentSettings.put("stripeAccountId", "");
        paymentSettings.put("stripeStatementDescriptor", "ATLAS");
        paymentSettings.put("paypalEnabled", Boolean.FALSE);
        paymentSettings.put("paypalClientId", "");
        paymentSettings.put("paypalSecret", "");
        paymentSettings.put("paypalWebhookId", "");
        paymentSettings.put("paypalMerchantId", "");
        paymentSettings.put("paypalStatementDescriptor", "ATLAS");
        paymentSettings.put("manualReviewEnabled", Boolean.TRUE);
        paymentSettings.put("saveCardsEnabled", Boolean.TRUE);
        siteSettings.setPaymentSettings(paymentSettings);

        Product product = new Product();
        product.setId(1L);
        product.setSlug("atlas-bottle");
        product.setName("Atlas Bottle");
        product.setPrice(BigDecimal.valueOf(42));
        product.setActive(true);

        BundleOffer bundleOffer = new BundleOffer();
        bundleOffer.setId(1L);
        bundleOffer.setName("Twin Pack");
        bundleOffer.setDescription("Desk and gym setup.");
        bundleOffer.setDiscountType("PERCENTAGE");
        bundleOffer.setDiscountValue(BigDecimal.TEN);
        bundleOffer.setBundleQuantity(2);
        bundleOffer.setPlacement("buy-box");
        bundleOffer.setActive(true);
        bundleOffer.setConfiguration(new HashMap<>(Map.of("badge", "Most popular")));

        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setId(1L);
        subscriptionPlan.setName("Monthly refresh");
        subscriptionPlan.setIntervalLabel("Every 30 days");
        subscriptionPlan.setFrequencyDays(30);
        subscriptionPlan.setDiscountPercent(5);
        subscriptionPlan.setActive(true);
        subscriptionPlan.setConfiguration(new HashMap<>(Map.of("description", "Save on each refill cycle.")));

        ContentPage contentPage = new ContentPage();
        contentPage.setId(1L);
        contentPage.setSlug("home");
        contentPage.setTitle("Home");
        contentPage.setPageType(PageType.HOMEPAGE);
        contentPage.setSeoMetadata(new HashMap<>(Map.of("title", "Atlas Home")));

        PageSection pageSection = new PageSection();
        pageSection.setId(1L);
        pageSection.setContentPage(contentPage);
        pageSection.setSectionKey("hero");
        pageSection.setDisplayName("Hero");
        pageSection.setSectionType("hero");
        pageSection.setPlacement("body");
        pageSection.setActive(true);
        pageSection.setPosition(1);
        pageSection.setMobilePosition(1);
        pageSection.setConfiguration(new HashMap<>(Map.of("headline", "Atlas hero")));

        FeatureFlag enabledFlag = new FeatureFlag();
        enabledFlag.setId(1L);
        enabledFlag.setFlagKey("sticky_add_to_cart");
        enabledFlag.setDisplayName("Sticky add to cart");
        enabledFlag.setModuleName("engagement");
        enabledFlag.setEnabled(true);
        enabledFlag.setDescription("Keep the primary checkout action visible on long product pages.");

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100001");
        customerOrder.setCustomerName("Morgan Lee");
        customerOrder.setEmail("morgan@example.test");
        customerOrder.setPaymentStatus(PaymentStatus.PAID);
        customerOrder.setFulfillmentStatus(FulfillmentStatus.UNFULFILLED);
        customerOrder.setTotalAmount(BigDecimal.valueOf(75.60));
        customerOrder.setLineItems(new ArrayList<>());
        customerOrder.setShipments(new ArrayList<>());

        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setProductName("Atlas Bottle");
        orderLineItem.setSku("ATLAS-20");
        orderLineItem.setQuantity(1);
        orderLineItem.setLineTotal(BigDecimal.valueOf(75.60));
        customerOrder.getLineItems().add(orderLineItem);

        when(siteSettingsService.getCurrentSettings()).thenReturn(siteSettings);
        when(storefrontProperties.getAssets()).thenReturn(assets);
        when(siteSettingsService.maskSecret(any())).thenReturn("****");
        when(featureFlagService.getFeatureFlagMap()).thenReturn(Map.of("sticky_add_to_cart", true));
        when(featureFlagService.listFeatureFlags()).thenReturn(List.of(enabledFlag));
        when(cartSessionService.getExistingCartToken(any())).thenReturn("cart-1");
        when(cartService.getCartItemCount(any())).thenReturn(0);
        when(themeAssetService.buildRenderView()).thenReturn(ThemeRenderView.empty());
        when(themeAssetService.countFiles()).thenReturn(10L);
        when(themeAssetService.listFiles()).thenReturn(List.of(
                new ThemeFileView("theme.css", "css", 512, Instant.now(), true, "/theme-assets/theme.css")
        ));
        when(themeAssetService.selectFile(any())).thenReturn("theme.css");
        when(themeAssetService.readFile(any())).thenReturn("body { color: #1d1a16; }");
        when(productRepository.count()).thenReturn(1L);
        when(contentPageRepository.count()).thenReturn(1L);
        when(pageSectionRepository.count()).thenReturn(1L);
        when(orderOperationsService.listOrders()).thenReturn(List.of(customerOrder));
        when(orderOperationsService.getDetailedOrder("WS-100001")).thenReturn(customerOrder);
        when(mediaAssetService.listAssets()).thenReturn(List.of(
                new MediaAssetDescriptor("atlas-bottle-hero.svg", "/media/atlas-bottle-hero.svg", 4096, Instant.now())
        ));
        when(catalogService.listProducts()).thenReturn(List.of(product));
        when(catalogService.getProductForAdmin(1L)).thenReturn(product);
        when(catalogService.listBundleOffers(1L)).thenReturn(List.of(bundleOffer));
        when(catalogService.listSubscriptionPlans(1L)).thenReturn(List.of(subscriptionPlan));
        when(pageManagementService.getPageBySlug("home")).thenReturn(contentPage);
        when(pageManagementService.listSections(1L)).thenReturn(List.of(pageSection));
        when(jsonContentService.writeJson(any())).thenAnswer(invocation ->
                invocation.getArgument(0) instanceof List<?> ? "[]" : "{}"
        );
    }

    @ParameterizedTest
    @MethodSource("adminRouteExpectations")
    void adminRoutesRenderExpectedTemplates(String path, String expectedView) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andExpect(content().string(containsString("admin-shell")))
                .andExpect(content().string(containsString("/assets/core/base.css")))
                .andExpect(content().string(containsString("/assets/admin/admin.css")))
                .andExpect(content().string(containsString("/assets/admin/admin.js")))
                .andExpect(content().string(containsString("?v=")));
    }

    @ParameterizedTest
    @MethodSource("dashboardExpectations")
    void dashboardUsesReadableThemeTokensAndGuidance(String path, String expectedView) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andExpect(content().string(containsString("--store-accent: #c85f35;")))
                .andExpect(content().string(not(containsString("\\#c85f35"))))
                .andExpect(content().string(containsString("Launch checklist")))
                .andExpect(content().string(containsString("Open catalog")));
    }

    private static Stream<Arguments> adminRouteExpectations() {
        return Stream.of(
                Arguments.of("/admin/site-settings", "admin/site-settings"),
                Arguments.of("/admin/payments", "admin/payment-settings"),
                Arguments.of("/admin/analytics", "admin/analytics"),
                Arguments.of("/admin/engagement", "admin/engagement"),
                Arguments.of("/admin/products", "admin/products"),
                Arguments.of("/admin/offers?productId=1", "admin/offers"),
                Arguments.of("/admin/orders", "admin/orders"),
                Arguments.of("/admin/orders/WS-100001", "admin/order-detail"),
                Arguments.of("/admin/media", "admin/media"),
                Arguments.of("/admin/pages/home", "admin/page-editor"),
                Arguments.of("/admin/themes", "admin/theme-studio")
        );
    }

    private static Stream<Arguments> dashboardExpectations() {
        return Stream.of(Arguments.of("/admin", "admin/dashboard"));
    }
}
