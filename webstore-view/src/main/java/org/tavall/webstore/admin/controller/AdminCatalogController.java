package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.admin.input.BundleOfferAdminInput;
import org.tavall.webstore.admin.input.ProductAdminInput;
import org.tavall.webstore.admin.input.SubscriptionPlanAdminInput;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.content.service.JsonContentService;

@Controller
public class AdminCatalogController {

    private final CatalogService catalogService;
    private final JsonContentService jsonContentService;

    public AdminCatalogController(CatalogService catalogService, JsonContentService jsonContentService) {
        this.catalogService = catalogService;
        this.jsonContentService = jsonContentService;
    }

    @GetMapping("/admin/products")
    public String products(Model model) {
        model.addAttribute("products", catalogService.listProducts());
        return "admin/products";
    }

    @GetMapping("/admin/products/new")
    public String newProduct(Model model) {
        model.addAttribute("productInput", new ProductAdminInput());
        return "admin/product-form";
    }

    @GetMapping("/admin/products/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        model.addAttribute("productInput", toProductInput(catalogService.getProductForAdmin(id)));
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String saveProduct(@ModelAttribute ProductAdminInput productAdminInput) {
        Product product = catalogService.saveProduct(productAdminInput);
        return "redirect:/admin/products/" + product.getId();
    }

    @GetMapping("/admin/offers")
    public String offers(@RequestParam(required = false) Long productId, Model model) {
        java.util.List<Product> products = catalogService.listProducts();
        model.addAttribute("products", products);

        if (products.isEmpty()) {
            model.addAttribute("product", null);
            model.addAttribute("bundleOffers", java.util.List.of());
            model.addAttribute("subscriptionPlans", java.util.List.of());
            model.addAttribute("selectedProductId", null);
            model.addAttribute("bundleOfferInput", new BundleOfferAdminInput());
            model.addAttribute("subscriptionPlanInput", new SubscriptionPlanAdminInput());
            return "admin/offers";
        }

        Long selectedProductId = resolveProductId(productId, products);
        Product product = catalogService.getProductForAdmin(selectedProductId);
        model.addAttribute("product", product);
        model.addAttribute("bundleOffers", catalogService.listBundleOffers(selectedProductId));
        model.addAttribute("subscriptionPlans", catalogService.listSubscriptionPlans(selectedProductId));
        model.addAttribute("selectedProductId", selectedProductId);
        model.addAttribute("bundleOfferInput", new BundleOfferAdminInput());
        model.addAttribute("subscriptionPlanInput", new SubscriptionPlanAdminInput());
        return "admin/offers";
    }

    @PostMapping("/admin/offers/bundles")
    public String saveBundleOffer(@ModelAttribute BundleOfferAdminInput bundleOfferAdminInput) {
        catalogService.saveBundleOffer(bundleOfferAdminInput);
        return "redirect:/admin/offers?productId=" + bundleOfferAdminInput.getProductId();
    }

    @PostMapping("/admin/offers/subscriptions")
    public String saveSubscriptionPlan(@ModelAttribute SubscriptionPlanAdminInput subscriptionPlanAdminInput) {
        catalogService.saveSubscriptionPlan(subscriptionPlanAdminInput);
        return "redirect:/admin/offers?productId=" + subscriptionPlanAdminInput.getProductId();
    }

    private Long resolveProductId(Long requestedProductId, java.util.List<Product> products) {
        if (requestedProductId != null && products.stream().anyMatch(product -> product.getId().equals(requestedProductId))) {
            return requestedProductId;
        }
        return products.getFirst().getId();
    }

    private ProductAdminInput toProductInput(Product product) {
        ProductAdminInput productAdminInput = new ProductAdminInput();
        productAdminInput.setId(product.getId());
        productAdminInput.setSlug(product.getSlug());
        productAdminInput.setName(product.getName());
        productAdminInput.setTagline(product.getTagline());
        productAdminInput.setShortDescription(product.getShortDescription());
        productAdminInput.setFullDescription(product.getFullDescription());
        productAdminInput.setActive(product.isActive());
        productAdminInput.setPrice(product.getPrice());
        productAdminInput.setCompareAtPrice(product.getCompareAtPrice());
        productAdminInput.setCurrency(product.getCurrency());
        productAdminInput.setSku(product.getSku());
        productAdminInput.setBarcode(product.getBarcode());
        productAdminInput.setInventoryTracking(product.isInventoryTracking());
        productAdminInput.setInventoryQuantity(product.getInventoryQuantity());
        productAdminInput.setSubscriptionAvailable(product.isSubscriptionAvailable());
        productAdminInput.setBundleEligible(product.isBundleEligible());
        productAdminInput.setShippingProfile(product.getShippingProfile());
        productAdminInput.setTaxCategory(product.getTaxCategory());
        productAdminInput.setMediaAssetsJson(jsonContentService.writeJson(product.getMediaAssets()));
        productAdminInput.setBadgesJson(jsonContentService.writeJson(product.getBadges()));
        productAdminInput.setHighlightsJson(jsonContentService.writeJson(product.getHighlights()));
        productAdminInput.setBenefitsJson(jsonContentService.writeJson(product.getBenefits()));
        productAdminInput.setFaqEntriesJson(jsonContentService.writeJson(product.getFaqEntries()));
        productAdminInput.setGuaranteeText(product.getGuaranteeText());
        productAdminInput.setReviewSummaryJson(jsonContentService.writeJson(product.getReviewSummary()));
        productAdminInput.setSeoMetadataJson(jsonContentService.writeJson(product.getSeoMetadata()));
        productAdminInput.setCustomAttributesJson(jsonContentService.writeJson(product.getCustomAttributes()));
        productAdminInput.setPresentationMetadataJson(jsonContentService.writeJson(product.getPresentationMetadata()));
        return productAdminInput;
    }
}
