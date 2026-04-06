package org.tavall.webstore.admin.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;
import org.tavall.webstore.cart.model.CartStatus;
import org.tavall.webstore.cart.model.ShoppingCart;
import org.tavall.webstore.cart.repository.ShoppingCartRepository;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.SubscriptionPlan;
import org.tavall.webstore.catalog.repository.BundleOfferRepository;
import org.tavall.webstore.catalog.repository.ProductRepository;
import org.tavall.webstore.catalog.repository.SubscriptionPlanRepository;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.model.CheckoutSessionStatus;
import org.tavall.webstore.checkout.repository.CheckoutSessionRepository;
import org.tavall.webstore.content.model.ContentPage;
import org.tavall.webstore.content.model.FeatureFlag;
import org.tavall.webstore.content.model.PageSection;
import org.tavall.webstore.content.model.PageType;
import org.tavall.webstore.content.model.SiteSettings;
import org.tavall.webstore.content.repository.ContentPageRepository;
import org.tavall.webstore.content.repository.FeatureFlagRepository;
import org.tavall.webstore.content.repository.PageSectionRepository;
import org.tavall.webstore.content.repository.SiteSettingsRepository;
import org.tavall.webstore.media.service.MediaAssetService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.FulfillmentStatus;
import org.tavall.webstore.orders.model.OrderLineItem;
import org.tavall.webstore.orders.model.OrderStatus;
import org.tavall.webstore.orders.model.PaymentStatus;
import org.tavall.webstore.orders.model.Shipment;
import org.tavall.webstore.orders.model.ShipmentStatus;
import org.tavall.webstore.orders.model.TrackingEvent;
import org.tavall.webstore.orders.repository.CustomerOrderRepository;
import org.tavall.webstore.theme.service.ThemeAssetService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    private static final Pattern ANCHOR_PATTERN = Pattern.compile("<a\\b[^>]*href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORM_PATTERN = Pattern.compile("<form\\b([^>]*)>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ACTION_PATTERN = Pattern.compile("\\baction=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern METHOD_PATTERN = Pattern.compile("\\bmethod=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern VISIBLE_JSON_LABEL_PATTERN = Pattern.compile(
            "<label[^>]*>[^<]*(json|configurationjson|settingsjson)",
            Pattern.CASE_INSENSITIVE
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Autowired
    private ContentPageRepository contentPageRepository;

    @Autowired
    private PageSectionRepository pageSectionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BundleOfferRepository bundleOfferRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CheckoutSessionRepository checkoutSessionRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private ThemeAssetService themeAssetService;

    @Autowired
    private MediaAssetService mediaAssetService;

    @BeforeEach
    void setUp() throws IOException {
        clearPersistentState();
        seedAdminFixture(true);
    }

    @Test
    void adminPagesReferenceReachableLinksAndMappedFormActions() throws Exception {
        LinkedHashSet<String> pagesToInspect = new LinkedHashSet<>(List.of(
                "/admin",
                "/admin/site-settings",
                "/admin/payments",
                "/admin/analytics",
                "/admin/engagement",
                "/admin/products",
                "/admin/offers",
                "/admin/orders",
                "/admin/media",
                "/admin/pages/home",
                "/admin/pages/product-default",
                "/admin/themes"
        ));
        Set<String> inspectedPages = new LinkedHashSet<>();
        Set<String> verifiedLinks = new LinkedHashSet<>();

        while (!pagesToInspect.isEmpty()) {
            String pagePath = pagesToInspect.iterator().next();
            pagesToInspect.remove(pagePath);
            if (!inspectedPages.add(pagePath)) {
                continue;
            }

            MvcResult pageResult = performGet(pagePath)
                    .andExpect(status().isOk())
                    .andReturn();
            String html = pageResult.getResponse().getContentAsString();

            assertThat(VISIBLE_JSON_LABEL_PATTERN.matcher(html).find())
                    .as("Visible JSON label found on %s", pagePath)
                    .isFalse();

            for (String href : extractAnchorTargets(html)) {
                if (!verifiedLinks.add(href)) {
                    continue;
                }
                performGet(href).andExpect(status().is2xxSuccessful());
                if (href.startsWith("/admin")) {
                    pagesToInspect.add(href);
                }
            }

            for (FormTarget formTarget : extractFormTargets(html)) {
                assertThat(hasHandlerMapping(formTarget.path(), formTarget.method()))
                        .as("No %s handler mapping for %s referenced from %s", formTarget.method(), formTarget.path(), pagePath)
                        .isTrue();
            }
        }
    }

    @Test
    void offersPageStillRendersWhenNoProductsExist() throws Exception {
        clearPersistentState();
        seedAdminFixture(false);

        MvcResult result = performGet("/admin/offers")
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html).contains("Create a product first");
        assertThat(html).contains("/admin/products/new");
    }

    @Test
    void assetCachePoliciesKeepAdminEditableAndStorefrontCacheable() throws Exception {
        mockMvc.perform(get("/assets/admin/admin.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));

        mockMvc.perform(get("/assets/core/base.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-cache")))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("public")));

        mockMvc.perform(get("/assets/storefront/storefront.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=")))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("immutable")));
    }

    private void clearPersistentState() throws IOException {
        customerOrderRepository.deleteAll();
        checkoutSessionRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        bundleOfferRepository.deleteAll();
        subscriptionPlanRepository.deleteAll();
        pageSectionRepository.deleteAll();
        contentPageRepository.deleteAll();
        featureFlagRepository.deleteAll();
        siteSettingsRepository.deleteAll();
        productRepository.deleteAll();

        Path storageRoot = Path.of("target/test-media");
        if (Files.exists(storageRoot)) {
            try (var paths = Files.walk(storageRoot)) {
                paths.sorted(java.util.Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException exception) {
                                throw new IllegalStateException("Unable to clear test media storage.", exception);
                            }
                        });
            }
        }
        Files.createDirectories(storageRoot);
    }

    private void seedAdminFixture(boolean includeProduct) throws IOException {
        SiteSettings siteSettings = new SiteSettings();
        siteSettings.setSiteName("Atlas Bottle");
        siteSettings.setLogoPath("/media/atlas-wordmark.svg");
        siteSettings.setFaviconPath("/media/atlas-wordmark.svg");
        siteSettings.setSupportEmail("support@atlasbottle.test");
        siteSettings.setHeaderCtaText("Shop Atlas");
        siteSettings.setShippingMessage("Free shipping on orders over $50 and carbon-neutral delivery.");
        siteSettings.setReturnMessage("Try it for 30 days and return it if it does not fit your routine.");
        siteSettings.setGuaranteeMessage("Backed by a no-drip guarantee and real support.");
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
        siteSettings.setSeoDefaults(Map.of(
                "titleTemplate", "%s | Atlas Bottle",
                "description", "A daily hydration system focused on conversion and clarity."
        ));
        siteSettings.setAnalyticsSettings(Map.of(
                "ga4Id", "G-ATLAS123",
                "metaPixelId", "1234567890",
                "conversionHooks", List.of("purchase", "begin_checkout")
        ));
        siteSettings.setPaymentSettings(Map.of(
                "stripeEnabled", true,
                "paypalEnabled", true,
                "mode", "sandbox",
                "defaultProvider", "STRIPE",
                "providerOrder", List.of("STRIPE", "PAYPAL"),
                "sandboxNotice", "Providers are simulated until real credentials are supplied."
        ));
        siteSettings.setReviewSource(Map.of(
                "label", "4.9 / 5 average",
                "source", "from verified buyers"
        ));
        siteSettings.setFooterContent(Map.of(
                "columns", List.of(Map.of(
                        "title", "Support",
                        "links", List.of(Map.of("label", "Track an order", "href", "/tracking"))
                )),
                "finePrint", "Atlas Bottle is a demo storefront engine."
        ));
        siteSettings.setAnnouncementBars(List.of(Map.of("label", "Spring launch", "message", "Save 10% with ATLAS10")));
        siteSettings.setPromoBanners(List.of(Map.of("headline", "Copper Clay ships this week")));
        siteSettings.setTrustBadges(List.of(Map.of("label", "Free shipping", "detail", "Orders over $50")));
        siteSettingsRepository.save(siteSettings);

        featureFlagRepository.saveAll(List.of(
                featureFlag("subscriptions_enabled", "Subscriptions", "Offers", true),
                featureFlag("bundles_enabled", "Bundles", "Offers", true),
                featureFlag("tracking_lookup_enabled", "Tracking lookup", "Operations", true)
        ));

        ContentPage homePage = new ContentPage();
        homePage.setSlug("home");
        homePage.setTitle("Atlas Bottle Homepage");
        homePage.setPageType(PageType.HOMEPAGE);
        homePage.setLayoutConfiguration(Map.of("heroLayout", "media-right"));
        homePage.setSeoMetadata(Map.of("title", "Atlas Bottle | Daily hydration without the leak chaos"));
        contentPageRepository.save(homePage);

        ContentPage productPage = new ContentPage();
        productPage.setSlug("product-default");
        productPage.setTitle("Atlas Bottle Product Page");
        productPage.setPageType(PageType.PRODUCT);
        productPage.setLayoutConfiguration(Map.of("stickyAddToCart", true));
        productPage.setSeoMetadata(Map.of("title", "Shop Atlas Bottle"));
        contentPageRepository.save(productPage);

        pageSectionRepository.save(pageSection(
                homePage,
                "hero",
                "Homepage Hero",
                "hero",
                "top",
                10,
                Map.of(
                        "eyebrow", "Hydration without the drip drama",
                        "title", "The insulated bottle built for desks and commutes.",
                        "body", "Atlas keeps coffee hot, water cold, and bags dry.",
                        "ctaLabel", "Shop the bottle",
                        "ctaHref", "/products/atlas-bottle"
                )
        ));
        pageSectionRepository.save(pageSection(
                productPage,
                "product-specs",
                "Product Specs",
                "specs",
                "body",
                20,
                Map.of(
                        "title", "Materials and daily details",
                        "items", List.of(Map.of("label", "Capacity", "value", "20 oz"))
                )
        ));

        themeAssetService.saveFile("theme.css", ":root { --theme-panel-radius: 28px; }");
        themeAssetService.saveFile("theme.js", "document.documentElement.dataset.themeReady = 'true';");

        mediaAssetService.store(new MockMultipartFile(
                "file",
                "atlas-wordmark.svg",
                "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 12 12\"><rect width=\"12\" height=\"12\" fill=\"#c85f35\"/></svg>"
                        .getBytes(StandardCharsets.UTF_8)
        ));

        if (!includeProduct) {
            return;
        }

        Product product = new Product();
        product.setSlug("atlas-bottle");
        product.setName("Atlas Bottle");
        product.setTagline("Premium daily bottle with a lock-top seal.");
        product.setShortDescription("A leak-proof insulated bottle sized for real commutes.");
        product.setFullDescription("Atlas Bottle is a reusable daily carry built around one job: keeping temperature and avoiding leaks.");
        product.setPrice(BigDecimal.valueOf(42));
        product.setCompareAtPrice(BigDecimal.valueOf(52));
        product.setCurrency("USD");
        product.setSku("ATLAS-BOTTLE-20OZ");
        product.setInventoryQuantity(128);
        product.setSubscriptionAvailable(true);
        product.setBundleEligible(true);
        product.setShippingProfile("STANDARD");
        product.setTaxCategory("GENERAL_MERCH");
        product.setMediaAssets(List.of(Map.of("path", "/media/atlas-wordmark.svg", "alt", "Atlas Bottle hero shot")));
        product.setBadges(List.of("Best Seller", "Leak-Proof"));
        product.setHighlights(List.of("Leak-proof lock lid", "18-hour cold hold"));
        product.setBenefits(List.of("Stops bag leaks", "Looks at home on your desk"));
        product.setFaqEntries(List.of(Map.of("question", "Is it dishwasher safe?", "answer", "Yes.")));
        product.setGuaranteeText("If it drips during normal use in the first 30 days, we replace it.");
        product.setReviewSummary(Map.of("rating", "4.9", "count", "4200"));
        product.setSeoMetadata(Map.of("title", "Atlas Bottle | Leak-proof insulated bottle"));
        product.setCustomAttributes(Map.of("materials", List.of("18/8 stainless steel")));
        product.setPresentationMetadata(Map.of("showQuantitySelector", true, "stickyAddToCart", true));
        productRepository.save(product);

        BundleOffer bundleOffer = new BundleOffer();
        bundleOffer.setProduct(product);
        bundleOffer.setName("Twin Pack");
        bundleOffer.setDescription("Two bottles for the desk-and-gym setup.");
        bundleOffer.setDiscountType("PERCENTAGE");
        bundleOffer.setDiscountValue(BigDecimal.valueOf(12));
        bundleOffer.setBundleQuantity(2);
        bundleOffer.setActive(true);
        bundleOffer.setPlacement("buy-box");
        bundleOffer.setConfiguration(Map.of("badge", "Most popular"));
        bundleOfferRepository.save(bundleOffer);

        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setProduct(product);
        subscriptionPlan.setName("Monthly refresh");
        subscriptionPlan.setIntervalLabel("Every 30 days");
        subscriptionPlan.setFrequencyDays(30);
        subscriptionPlan.setDiscountPercent(8);
        subscriptionPlan.setActive(true);
        subscriptionPlan.setConfiguration(Map.of("description", "Ideal if you rotate bottles or gift them regularly."));
        subscriptionPlanRepository.save(subscriptionPlan);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setToken("seed-cart");
        shoppingCart.setStatus(CartStatus.CONVERTED);
        shoppingCart.setCurrency("USD");
        shoppingCart.setEmail("customer@example.com");
        shoppingCart.setSubtotalAmount(BigDecimal.valueOf(84));
        shoppingCart.setDiscountAmount(BigDecimal.valueOf(8.40));
        shoppingCart.setShippingAmount(BigDecimal.ZERO);
        shoppingCart.setTaxAmount(BigDecimal.ZERO);
        shoppingCart.setTotalAmount(BigDecimal.valueOf(75.60));
        shoppingCart.setCartAttributes(Map.of("source", "integration-test"));
        shoppingCartRepository.save(shoppingCart);

        CheckoutSession checkoutSession = new CheckoutSession();
        checkoutSession.setCart(shoppingCart);
        checkoutSession.setProvider(CheckoutProvider.STRIPE);
        checkoutSession.setStatus(CheckoutSessionStatus.COMPLETED);
        checkoutSession.setExternalReference("seed-checkout-1");
        checkoutSession.setAmount(BigDecimal.valueOf(75.60));
        checkoutSession.setCurrency("USD");
        checkoutSession.setRedirectUrl("/checkout/sessions/seed-checkout-1");
        checkoutSession.setCheckoutData(Map.of(
                "customerName", "Morgan Lee",
                "email", "customer@example.com",
                "addressLine1", "44 Market Street",
                "city", "Oakland",
                "state", "CA",
                "postalCode", "94607",
                "country", "US"
        ));
        checkoutSessionRepository.save(checkoutSession);

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100001");
        customerOrder.setCheckoutSession(checkoutSession);
        customerOrder.setStatus(OrderStatus.FULFILLED);
        customerOrder.setPaymentStatus(PaymentStatus.PAID);
        customerOrder.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        customerOrder.setEmail("customer@example.com");
        customerOrder.setCustomerName("Morgan Lee");
        customerOrder.setCurrency("USD");
        customerOrder.setSubtotalAmount(BigDecimal.valueOf(84));
        customerOrder.setDiscountAmount(BigDecimal.valueOf(8.40));
        customerOrder.setShippingAmount(BigDecimal.ZERO);
        customerOrder.setTaxAmount(BigDecimal.ZERO);
        customerOrder.setTotalAmount(BigDecimal.valueOf(75.60));
        customerOrder.setDiscountCode("ATLAS10");
        customerOrder.setShippingAddress(Map.of(
                "addressLine1", "44 Market Street",
                "city", "Oakland",
                "state", "CA",
                "postalCode", "94607",
                "country", "US"
        ));
        customerOrder.setBillingAddress(Map.of(
                "addressLine1", "44 Market Street",
                "city", "Oakland",
                "state", "CA",
                "postalCode", "94607",
                "country", "US"
        ));
        customerOrder.setOrderAttributes(Map.of("notes", "Integration seeded order"));

        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setCustomerOrder(customerOrder);
        orderLineItem.setProduct(product);
        orderLineItem.setProductName(product.getName());
        orderLineItem.setSku(product.getSku());
        orderLineItem.setQuantity(2);
        orderLineItem.setUnitPrice(BigDecimal.valueOf(42));
        orderLineItem.setLineTotal(BigDecimal.valueOf(84));
        customerOrder.getLineItems().add(orderLineItem);

        Shipment shipment = new Shipment();
        shipment.setCustomerOrder(customerOrder);
        shipment.setCarrier("UPS");
        shipment.setTrackingNumber("1Z999AA10123456784");
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setShippedAt(Instant.now().minusSeconds(5 * 24 * 60 * 60));
        shipment.setDeliveredAt(Instant.now().minusSeconds(2 * 24 * 60 * 60));
        shipment.setTrackingPayload(Map.of("carrierUrl", "https://example.test/ups/1Z999AA10123456784"));

        TrackingEvent trackingEvent = new TrackingEvent();
        trackingEvent.setShipment(shipment);
        trackingEvent.setEventTimestamp(Instant.now().minusSeconds(2 * 24 * 60 * 60));
        trackingEvent.setStatus("Delivered");
        trackingEvent.setLocation("Oakland, CA");
        trackingEvent.setMessage("Delivered to front desk.");
        trackingEvent.setEventPayload(Map.of("status", "Delivered"));
        shipment.getTrackingEvents().add(trackingEvent);
        customerOrder.getShipments().add(shipment);

        customerOrderRepository.save(customerOrder);
    }

    private FeatureFlag featureFlag(String key, String displayName, String moduleName, boolean enabled) {
        FeatureFlag featureFlag = new FeatureFlag();
        featureFlag.setFlagKey(key);
        featureFlag.setDisplayName(displayName);
        featureFlag.setModuleName(moduleName);
        featureFlag.setEnabled(enabled);
        featureFlag.setDescription("Integration seeded flag for " + displayName + ".");
        return featureFlag;
    }

    private PageSection pageSection(
            ContentPage contentPage,
            String sectionKey,
            String displayName,
            String sectionType,
            String placement,
            int position,
            Map<String, Object> configuration
    ) {
        PageSection pageSection = new PageSection();
        pageSection.setContentPage(contentPage);
        pageSection.setSectionKey(sectionKey);
        pageSection.setDisplayName(displayName);
        pageSection.setSectionType(sectionType);
        pageSection.setPlacement(placement);
        pageSection.setActive(true);
        pageSection.setPosition(position);
        pageSection.setMobilePosition(position);
        pageSection.setConfiguration(configuration);
        return pageSection;
    }

    private List<String> extractAnchorTargets(String html) {
        List<String> hrefs = new ArrayList<>();
        Matcher matcher = ANCHOR_PATTERN.matcher(html);
        while (matcher.find()) {
            String href = matcher.group(1);
            if (href.startsWith("/") && !href.startsWith("//")) {
                hrefs.add(href);
            }
        }
        return hrefs;
    }

    private List<FormTarget> extractFormTargets(String html) {
        List<FormTarget> formTargets = new ArrayList<>();
        Matcher matcher = FORM_PATTERN.matcher(html);
        while (matcher.find()) {
            String attributes = matcher.group(1);
            Matcher actionMatcher = ACTION_PATTERN.matcher(attributes);
            if (!actionMatcher.find()) {
                continue;
            }
            Matcher methodMatcher = METHOD_PATTERN.matcher(attributes);
            String method = methodMatcher.find() ? methodMatcher.group(1).toUpperCase(Locale.ROOT) : "GET";
            formTargets.add(new FormTarget(actionMatcher.group(1), method));
        }
        return formTargets;
    }

    private boolean hasHandlerMapping(String rawPath, String httpMethod) {
        URI uri = URI.create("http://localhost" + rawPath);
        String path = uri.getPath();
        RequestMethod requestMethod = RequestMethod.valueOf(httpMethod.toUpperCase(Locale.ROOT));
        PathContainer pathContainer = PathContainer.parsePath(path);

        for (RequestMappingInfo requestMappingInfo : requestMappingHandlerMapping.getHandlerMethods().keySet()) {
            Set<RequestMethod> declaredMethods = requestMappingInfo.getMethodsCondition().getMethods();
            boolean methodMatches = declaredMethods.isEmpty() || declaredMethods.contains(requestMethod);
            if (!methodMatches) {
                continue;
            }

            boolean pathMatches = requestMappingInfo.getPatternValues().stream()
                    .anyMatch(pattern -> PathPatternParser.defaultInstance.parse(pattern).matches(pathContainer));
            if (pathMatches) {
                return true;
            }
        }

        return false;
    }

    private org.springframework.test.web.servlet.ResultActions performGet(String rawPath) throws Exception {
        URI uri = URI.create("http://localhost" + rawPath);
        MockHttpServletRequestBuilder requestBuilder = get(uri.getPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            for (String pair : uri.getRawQuery().split("&")) {
                String[] pieces = pair.split("=", 2);
                String key = URLDecoder.decode(pieces[0], StandardCharsets.UTF_8);
                String value = pieces.length > 1 ? URLDecoder.decode(pieces[1], StandardCharsets.UTF_8) : "";
                requestBuilder.queryParam(key, value);
            }
        }
        return mockMvc.perform(requestBuilder);
    }

    private record FormTarget(String path, String method) {
    }
}
