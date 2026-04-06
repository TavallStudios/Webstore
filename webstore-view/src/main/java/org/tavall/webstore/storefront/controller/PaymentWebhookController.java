package org.tavall.webstore.storefront.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.payments.service.PaymentWebhookService;

@RestController
@RequestMapping("/webhooks")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, Object>> processStripeWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(processWebhook(CheckoutProvider.STRIPE, payload));
    }

    @PostMapping("/paypal")
    public ResponseEntity<Map<String, Object>> processPayPalWebhook(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(processWebhook(CheckoutProvider.PAYPAL, payload));
    }

    private Map<String, Object> processWebhook(CheckoutProvider provider, Map<String, Object> payload) {
        CustomerOrder customerOrder = paymentWebhookService.processPaymentEvent(
                provider,
                String.valueOf(payload.getOrDefault("eventId", "manual-event")),
                String.valueOf(payload.get("checkoutReference")),
                String.valueOf(payload.getOrDefault("paymentReference", "manual-payment")),
                payload
        );
        return Map.of(
                "status", "processed",
                "provider", provider.name(),
                "orderNumber", customerOrder.getOrderNumber()
        );
    }
}
