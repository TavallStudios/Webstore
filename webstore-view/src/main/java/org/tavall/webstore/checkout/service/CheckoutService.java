package org.tavall.webstore.checkout.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.admin.input.CheckoutRequestInput;
import org.tavall.webstore.cart.model.ShoppingCart;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.catalog.service.PromotionService;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.model.CheckoutSessionStatus;
import org.tavall.webstore.checkout.repository.CheckoutSessionRepository;
import org.tavall.webstore.content.service.SiteSettingsService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.payments.service.PaymentWebhookService;

@Service
public class CheckoutService {

    private final CartService cartService;
    private final PromotionService promotionService;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final PaymentWebhookService paymentWebhookService;
    private final SiteSettingsService siteSettingsService;

    public CheckoutService(
            CartService cartService,
            PromotionService promotionService,
            CheckoutSessionRepository checkoutSessionRepository,
            PaymentWebhookService paymentWebhookService,
            SiteSettingsService siteSettingsService
    ) {
        this.cartService = cartService;
        this.promotionService = promotionService;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.paymentWebhookService = paymentWebhookService;
        this.siteSettingsService = siteSettingsService;
    }

    @Transactional
    public CheckoutSession createCheckoutSession(String cartToken, CheckoutRequestInput input) {
        ShoppingCart shoppingCart = cartService.loadOrCreateActiveCart(cartToken);
        if (shoppingCart.getLineItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot check out with an empty cart.");
        }

        BigDecimal discountAmount = promotionService.calculateDiscount(shoppingCart.getSubtotalAmount(), input.getDiscountCode());
        BigDecimal totalAmount = shoppingCart.getSubtotalAmount()
                .subtract(discountAmount)
                .add(shoppingCart.getShippingAmount())
                .add(shoppingCart.getTaxAmount());

        shoppingCart.setEmail(input.getEmail());
        CheckoutProvider checkoutProvider = CheckoutProvider.valueOf(input.getProvider().toUpperCase());
        ensureProviderEnabled(checkoutProvider);
        CheckoutSession checkoutSession = new CheckoutSession();
        checkoutSession.setCart(shoppingCart);
        checkoutSession.setProvider(checkoutProvider);
        checkoutSession.setStatus(CheckoutSessionStatus.PENDING);
        checkoutSession.setExternalReference("chk_" + UUID.randomUUID().toString().replace("-", ""));
        checkoutSession.setAmount(totalAmount);
        checkoutSession.setCurrency(shoppingCart.getCurrency());
        checkoutSession.setRedirectUrl("/checkout/sessions/" + checkoutSession.getExternalReference());
        checkoutSession.setCheckoutData(buildCheckoutData(input, discountAmount, shoppingCart));
        return checkoutSessionRepository.save(checkoutSession);
    }

    @Transactional(readOnly = true)
    public CheckoutSession getCheckoutSession(String externalReference) {
        return checkoutSessionRepository.findDetailedByExternalReference(externalReference)
                .orElseThrow(() -> new IllegalArgumentException("Unknown checkout session: " + externalReference));
    }

    @Transactional
    public CustomerOrder simulateSuccessfulCheckout(String externalReference) {
        CheckoutSession checkoutSession = getCheckoutSession(externalReference);
        return paymentWebhookService.processPaymentEvent(
                checkoutSession.getProvider(),
                "sim_" + UUID.randomUUID(),
                externalReference,
                "pay_" + UUID.randomUUID(),
                Map.of("simulated", true, "status", "paid")
        );
    }

    private Map<String, Object> buildCheckoutData(CheckoutRequestInput input, BigDecimal discountAmount, ShoppingCart shoppingCart) {
        Map<String, Object> checkoutData = new HashMap<>();
        checkoutData.put("customerName", input.getCustomerName());
        checkoutData.put("email", input.getEmail());
        checkoutData.put("addressLine1", input.getAddressLine1());
        checkoutData.put("addressLine2", input.getAddressLine2());
        checkoutData.put("city", input.getCity());
        checkoutData.put("state", input.getState());
        checkoutData.put("postalCode", input.getPostalCode());
        checkoutData.put("country", input.getCountry());
        checkoutData.put("discountCode", input.getDiscountCode());
        checkoutData.put("discountAmount", discountAmount);
        checkoutData.put("shippingAmount", shoppingCart.getShippingAmount());
        checkoutData.put("taxAmount", shoppingCart.getTaxAmount());
        return checkoutData;
    }

    private void ensureProviderEnabled(CheckoutProvider checkoutProvider) {
        java.util.Map<String, Object> paymentSettings = siteSettingsService.getCurrentSettings().getPaymentSettings();
        boolean enabled = switch (checkoutProvider) {
            case STRIPE -> Boolean.TRUE.equals(paymentSettings.getOrDefault("stripeEnabled", Boolean.TRUE));
            case PAYPAL -> Boolean.TRUE.equals(paymentSettings.getOrDefault("paypalEnabled", Boolean.TRUE));
        };
        if (!enabled) {
            throw new IllegalArgumentException("The selected payment provider is not enabled.");
        }
    }
}
