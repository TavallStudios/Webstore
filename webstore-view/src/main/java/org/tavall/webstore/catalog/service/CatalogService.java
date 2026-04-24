package org.tavall.webstore.catalog.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.tavall.webstore.admin.input.BundleOfferAdminInput;
import org.tavall.webstore.admin.input.ProductAdminInput;
import org.tavall.webstore.admin.input.SubscriptionPlanAdminInput;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.ProductVariant;
import org.tavall.webstore.catalog.model.Promotion;
import org.tavall.webstore.catalog.model.SubscriptionPlan;
import org.tavall.webstore.catalog.repository.BundleOfferRepository;
import org.tavall.webstore.catalog.repository.ProductRepository;
import org.tavall.webstore.catalog.repository.PromotionRepository;
import org.tavall.webstore.catalog.repository.SubscriptionPlanRepository;
import org.tavall.webstore.content.service.JsonContentService;

@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final BundleOfferRepository bundleOfferRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PromotionRepository promotionRepository;
    private final JsonContentService jsonContentService;

    public CatalogService(
            ProductRepository productRepository,
            BundleOfferRepository bundleOfferRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            PromotionRepository promotionRepository,
            JsonContentService jsonContentService
    ) {
        this.productRepository = productRepository;
        this.bundleOfferRepository = bundleOfferRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.promotionRepository = promotionRepository;
        this.jsonContentService = jsonContentService;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getFeaturedProduct() {
        return productRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active products configured."));
    }

    @Transactional(readOnly = true)
    public Product getProductForStorefront(String slug) {
        Product product = productRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new IllegalArgumentException("Unknown active product slug: " + slug));
        initializeProductCollections(product);
        return product;
    }

    @Transactional(readOnly = true)
    public Product getProductForAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown product id: " + id));
        initializeProductCollections(product);
        return product;
    }

    @Transactional(readOnly = true)
    public List<Promotion> listActivePromotions() {
        return promotionRepository.findAllByActiveTrueOrderByAutomaticDescDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public List<BundleOffer> listBundleOffers(Long productId) {
        return bundleOfferRepository.findAllByProductIdOrderByIdAsc(productId);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> listSubscriptionPlans(Long productId) {
        return subscriptionPlanRepository.findAllByProductIdOrderByIdAsc(productId);
    }

    @Transactional
    public Product saveProduct(ProductAdminInput input) {
        Product product = input.getId() == null
                ? new Product()
                : productRepository.findById(input.getId()).orElseThrow(() -> new IllegalArgumentException("Unknown product."));
        product.setSlug(input.getSlug());
        product.setName(input.getName());
        product.setTagline(input.getTagline());
        product.setShortDescription(input.getShortDescription());
        product.setFullDescription(input.getFullDescription());
        product.setActive(input.isActive());
        product.setPrice(input.getPrice() == null ? BigDecimal.ZERO : input.getPrice());
        product.setCompareAtPrice(input.getCompareAtPrice());
        product.setCurrency(input.getCurrency());
        product.setSku(input.getSku());
        product.setBarcode(input.getBarcode());
        product.setInventoryTracking(input.isInventoryTracking());
        product.setInventoryQuantity(input.getInventoryQuantity());
        product.setSubscriptionAvailable(input.isSubscriptionAvailable());
        product.setBundleEligible(input.isBundleEligible());
        product.setShippingProfile(input.getShippingProfile());
        product.setTaxCategory(input.getTaxCategory());
        product.setMediaAssets(jsonContentService.parseObjectList(input.getMediaAssetsJson()));
        product.setBadges(jsonContentService.parseStringList(input.getBadgesJson()));
        product.setHighlights(jsonContentService.parseStringList(input.getHighlightsJson()));
        product.setBenefits(jsonContentService.parseStringList(input.getBenefitsJson()));
        product.setFaqEntries(jsonContentService.parseObjectList(input.getFaqEntriesJson()));
        product.setGuaranteeText(input.getGuaranteeText());
        product.setReviewSummary(new HashMap<>(jsonContentService.parseObject(input.getReviewSummaryJson())));
        product.setSeoMetadata(new HashMap<>(jsonContentService.parseObject(input.getSeoMetadataJson())));
        product.setCustomAttributes(new HashMap<>(jsonContentService.parseObject(input.getCustomAttributesJson())));
        product.setPresentationMetadata(new HashMap<>(jsonContentService.parseObject(input.getPresentationMetadataJson())));
        return productRepository.save(product);
    }

    @Transactional
    public BundleOffer saveBundleOffer(BundleOfferAdminInput input) {
        BundleOffer bundleOffer = input.getId() == null
                ? new BundleOffer()
                : bundleOfferRepository.findById(input.getId()).orElseThrow(() -> new IllegalArgumentException("Unknown bundle offer."));
        Product product = productRepository.findById(input.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product."));
        bundleOffer.setProduct(product);
        bundleOffer.setName(input.getName());
        bundleOffer.setDescription(input.getDescription());
        bundleOffer.setDiscountType(input.getDiscountType());
        bundleOffer.setDiscountValue(input.getDiscountValue() == null ? BigDecimal.ZERO : input.getDiscountValue());
        bundleOffer.setBundleQuantity(input.getBundleQuantity());
        bundleOffer.setActive(input.isActive());
        bundleOffer.setPlacement(input.getPlacement());
        bundleOffer.setConfiguration(new HashMap<>(jsonContentService.parseObject(input.getConfigurationJson())));
        return bundleOfferRepository.save(bundleOffer);
    }

    @Transactional
    public SubscriptionPlan saveSubscriptionPlan(SubscriptionPlanAdminInput input) {
        SubscriptionPlan subscriptionPlan = input.getId() == null
                ? new SubscriptionPlan()
                : subscriptionPlanRepository.findById(input.getId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown subscription plan."));
        Product product = productRepository.findById(input.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown product."));
        subscriptionPlan.setProduct(product);
        subscriptionPlan.setName(input.getName());
        subscriptionPlan.setIntervalLabel(input.getIntervalLabel());
        subscriptionPlan.setFrequencyDays(input.getFrequencyDays());
        subscriptionPlan.setDiscountPercent(input.getDiscountPercent());
        subscriptionPlan.setActive(input.isActive());
        subscriptionPlan.setConfiguration(new HashMap<>(jsonContentService.parseObject(input.getConfigurationJson())));
        return subscriptionPlanRepository.save(subscriptionPlan);
    }

    @Transactional(readOnly = true)
    public ProductVariant getVariant(Product product, Long variantId) {
        if (variantId == null) {
            return null;
        }
        return product.getVariants().stream()
                .filter(ProductVariant::isActive)
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown product variant."));
    }

    @Transactional(readOnly = true)
    public BundleOffer getBundleOffer(Product product, Long bundleOfferId) {
        if (bundleOfferId == null) {
            return null;
        }
        return product.getBundleOffers().stream()
                .filter(BundleOffer::isActive)
                .filter(bundleOffer -> bundleOffer.getId().equals(bundleOfferId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown bundle offer."));
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getSubscriptionPlan(Product product, Long subscriptionPlanId) {
        if (subscriptionPlanId == null) {
            return null;
        }
        return product.getSubscriptionPlans().stream()
                .filter(SubscriptionPlan::isActive)
                .filter(subscriptionPlan -> subscriptionPlan.getId().equals(subscriptionPlanId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown subscription plan."));
    }

    private void initializeProductCollections(Product product) {
        Hibernate.initialize(product.getVariants());
        Hibernate.initialize(product.getBundleOffers());
        Hibernate.initialize(product.getSubscriptionPlans());
        Hibernate.initialize(product.getUpsellOffers());
    }
}
