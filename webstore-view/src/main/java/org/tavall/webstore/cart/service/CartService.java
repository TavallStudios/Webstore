package org.tavall.webstore.cart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.cart.model.CartLineItem;
import org.tavall.webstore.cart.model.CartStatus;
import org.tavall.webstore.cart.model.ShoppingCart;
import org.tavall.webstore.cart.repository.CartLineItemRepository;
import org.tavall.webstore.cart.repository.ShoppingCartRepository;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.model.ProductVariant;
import org.tavall.webstore.catalog.model.SubscriptionPlan;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.catalog.service.PromotionService;

@Service
public class CartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartLineItemRepository cartLineItemRepository;
    private final CatalogService catalogService;
    private final PromotionService promotionService;

    public CartService(
            ShoppingCartRepository shoppingCartRepository,
            CartLineItemRepository cartLineItemRepository,
            CatalogService catalogService,
            PromotionService promotionService
    ) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartLineItemRepository = cartLineItemRepository;
        this.catalogService = catalogService;
        this.promotionService = promotionService;
    }

    @Transactional(readOnly = true)
    public ShoppingCart getActiveCart(String cartToken) {
        if (cartToken == null || cartToken.isBlank()) {
            return null;
        }
        return shoppingCartRepository.findDetailedByTokenAndStatus(cartToken, CartStatus.ACTIVE).orElse(null);
    }

    @Transactional
    public ShoppingCart loadOrCreateActiveCart(String cartToken) {
        return shoppingCartRepository.findDetailedByTokenAndStatus(cartToken, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(cartToken));
    }

    @Transactional
    public ShoppingCart addProductToCart(
            String cartToken,
            Long productId,
            Long variantId,
            Integer requestedQuantity,
            Long bundleOfferId,
            Long subscriptionPlanId
    ) {
        ShoppingCart shoppingCart = loadOrCreateActiveCart(cartToken);
        Product product = catalogService.getProductForAdmin(productId);
        ProductVariant productVariant = catalogService.getVariant(product, variantId);
        BundleOffer bundleOffer = catalogService.getBundleOffer(product, bundleOfferId);
        SubscriptionPlan subscriptionPlan = catalogService.getSubscriptionPlan(product, subscriptionPlanId);
        int quantity = Math.max(1, requestedQuantity == null ? 1 : requestedQuantity);
        if (bundleOffer != null) {
            quantity = Math.max(quantity, bundleOffer.getBundleQuantity());
        }

        CartLineItem cartLineItem = new CartLineItem();
        cartLineItem.setCart(shoppingCart);
        cartLineItem.setProduct(product);
        cartLineItem.setVariant(productVariant);
        cartLineItem.setQuantity(quantity);
        cartLineItem.setProductNameSnapshot(product.getName());
        cartLineItem.setSkuSnapshot(productVariant != null ? productVariant.getSku() : product.getSku());
        cartLineItem.setPurchaseType(subscriptionPlan == null ? "ONE_TIME" : "SUBSCRIPTION");
        cartLineItem.setUnitPrice(calculateUnitPrice(product, productVariant, bundleOffer, subscriptionPlan, quantity));
        cartLineItem.setLineTotal(cartLineItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartLineItem.setLineAttributes(buildLineAttributes(bundleOffer, subscriptionPlan));
        shoppingCart.getLineItems().add(cartLineItem);
        cartLineItemRepository.save(cartLineItem);
        return recalculateAndSaveCart(shoppingCart);
    }

    @Transactional
    public ShoppingCart updateCartLineQuantity(String cartToken, Long cartLineItemId, int quantity) {
        ShoppingCart shoppingCart = loadOrCreateActiveCart(cartToken);
        CartLineItem cartLineItem = cartLineItemRepository.findById(cartLineItemId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown cart line item."));
        if (!cartLineItem.getCart().getId().equals(shoppingCart.getId())) {
            throw new IllegalArgumentException("Cart line item does not belong to the active cart.");
        }

        if (quantity <= 0) {
            shoppingCart.getLineItems().remove(cartLineItem);
            cartLineItemRepository.delete(cartLineItem);
            return recalculateAndSaveCart(shoppingCart);
        }

        cartLineItem.setQuantity(quantity);
        cartLineItem.setLineTotal(cartLineItem.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartLineItemRepository.save(cartLineItem);
        return recalculateAndSaveCart(shoppingCart);
    }

    @Transactional
    public void removeCartLine(String cartToken, Long cartLineItemId) {
        updateCartLineQuantity(cartToken, cartLineItemId, 0);
    }

    @Transactional(readOnly = true)
    public int getCartItemCount(String cartToken) {
        ShoppingCart shoppingCart = getActiveCart(cartToken);
        if (shoppingCart == null) {
            return 0;
        }
        return shoppingCart.getLineItems().stream().mapToInt(CartLineItem::getQuantity).sum();
    }

    @Transactional
    public ShoppingCart markConverted(ShoppingCart shoppingCart) {
        shoppingCart.setStatus(CartStatus.CONVERTED);
        return shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCart createNewCart(String cartToken) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setToken((cartToken == null || cartToken.isBlank()) ? "cart-" + UUID.randomUUID() : cartToken);
        shoppingCart.setStatus(CartStatus.ACTIVE);
        shoppingCart.setCurrency("USD");
        return shoppingCartRepository.save(shoppingCart);
    }

    private BigDecimal calculateUnitPrice(
            Product product,
            ProductVariant productVariant,
            BundleOffer bundleOffer,
            SubscriptionPlan subscriptionPlan,
            int quantity
    ) {
        BigDecimal basePrice = productVariant != null && productVariant.getPriceOverride() != null
                ? productVariant.getPriceOverride()
                : product.getPrice();

        if (bundleOffer != null) {
            if ("PERCENTAGE".equalsIgnoreCase(bundleOffer.getDiscountType())) {
                basePrice = basePrice.multiply(BigDecimal.valueOf(100).subtract(bundleOffer.getDiscountValue()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if ("FIXED".equalsIgnoreCase(bundleOffer.getDiscountType()) && quantity > 0) {
                BigDecimal bundleDiscountPerUnit = bundleOffer.getDiscountValue()
                        .divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
                basePrice = basePrice.subtract(bundleDiscountPerUnit).max(BigDecimal.ZERO);
            }
        }

        if (subscriptionPlan != null && subscriptionPlan.getDiscountPercent() > 0) {
            basePrice = basePrice.multiply(BigDecimal.valueOf(100 - subscriptionPlan.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return basePrice;
    }

    private Map<String, Object> buildLineAttributes(BundleOffer bundleOffer, SubscriptionPlan subscriptionPlan) {
        Map<String, Object> lineAttributes = new HashMap<>();
        if (bundleOffer != null) {
            lineAttributes.put("bundleOfferId", bundleOffer.getId());
            lineAttributes.put("bundleOfferName", bundleOffer.getName());
        }
        if (subscriptionPlan != null) {
            lineAttributes.put("subscriptionPlanId", subscriptionPlan.getId());
            lineAttributes.put("subscriptionPlanName", subscriptionPlan.getName());
        }
        return lineAttributes;
    }

    private ShoppingCart recalculateAndSaveCart(ShoppingCart shoppingCart) {
        BigDecimal subtotal = shoppingCart.getLineItems().stream()
                .map(CartLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int quantity = shoppingCart.getLineItems().stream().mapToInt(CartLineItem::getQuantity).sum();
        BigDecimal shippingAmount = promotionService.qualifiesForFreeShipping(quantity, subtotal)
                ? BigDecimal.ZERO
                : (subtotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(7.95));
        shoppingCart.setSubtotalAmount(subtotal);
        shoppingCart.setDiscountAmount(BigDecimal.ZERO);
        shoppingCart.setShippingAmount(shippingAmount);
        shoppingCart.setTaxAmount(BigDecimal.ZERO);
        shoppingCart.setTotalAmount(subtotal.add(shippingAmount));
        return shoppingCartRepository.save(shoppingCart);
    }
}
