package org.tavall.webstore.payments.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.model.CheckoutSessionStatus;
import org.tavall.webstore.checkout.model.WebhookEventLog;
import org.tavall.webstore.checkout.repository.CheckoutSessionRepository;
import org.tavall.webstore.checkout.repository.WebhookEventLogRepository;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.OrderStatus;
import org.tavall.webstore.orders.model.PaymentRecord;
import org.tavall.webstore.orders.model.PaymentStatus;
import org.tavall.webstore.orders.repository.CustomerOrderRepository;
import org.tavall.webstore.orders.repository.PaymentRecordRepository;
import org.tavall.webstore.orders.service.OrderOperationsService;

@Service
public class PaymentWebhookService {

    private final WebhookEventLogRepository webhookEventLogRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final OrderOperationsService orderOperationsService;
    private final CustomerOrderRepository customerOrderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final CartService cartService;

    public PaymentWebhookService(
            WebhookEventLogRepository webhookEventLogRepository,
            CheckoutSessionRepository checkoutSessionRepository,
            OrderOperationsService orderOperationsService,
            CustomerOrderRepository customerOrderRepository,
            PaymentRecordRepository paymentRecordRepository,
            CartService cartService
    ) {
        this.webhookEventLogRepository = webhookEventLogRepository;
        this.checkoutSessionRepository = checkoutSessionRepository;
        this.orderOperationsService = orderOperationsService;
        this.customerOrderRepository = customerOrderRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.cartService = cartService;
    }

    @Transactional
    public CustomerOrder processPaymentEvent(
            CheckoutProvider provider,
            String eventId,
            String externalReference,
            String externalPaymentReference,
            Map<String, Object> payload
    ) {
        CheckoutSession checkoutSession = checkoutSessionRepository.findDetailedByExternalReference(externalReference)
                .orElseThrow(() -> new IllegalArgumentException("Unknown checkout session reference: " + externalReference));

        if (webhookEventLogRepository.existsByProviderAndEventId(provider, eventId)) {
            return customerOrderRepository.findByCheckoutSessionId(checkoutSession.getId())
                    .orElseGet(() -> orderOperationsService.createOrderFromCheckoutSession(checkoutSession));
        }

        WebhookEventLog webhookEventLog = new WebhookEventLog();
        webhookEventLog.setProvider(provider);
        webhookEventLog.setEventId(eventId);
        webhookEventLog.setEventType("payment.succeeded");
        webhookEventLog.setPayload(new HashMap<>(payload));
        webhookEventLogRepository.save(webhookEventLog);

        checkoutSession.setStatus(CheckoutSessionStatus.COMPLETED);
        checkoutSessionRepository.save(checkoutSession);

        CustomerOrder customerOrder = orderOperationsService.createOrderFromCheckoutSession(checkoutSession);
        customerOrder.setStatus(OrderStatus.PAID);
        customerOrder.setPaymentStatus(PaymentStatus.PAID);
        customerOrderRepository.save(customerOrder);

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setCustomerOrder(customerOrder);
        paymentRecord.setCheckoutSession(checkoutSession);
        paymentRecord.setProvider(provider);
        paymentRecord.setPaymentStatus(PaymentStatus.PAID);
        paymentRecord.setExternalPaymentReference(externalPaymentReference);
        paymentRecord.setProviderEventId(eventId);
        paymentRecord.setAmount(checkoutSession.getAmount());
        paymentRecord.setCurrency(checkoutSession.getCurrency());
        paymentRecord.setProviderPayload(new HashMap<>(payload));
        paymentRecordRepository.save(paymentRecord);

        cartService.markConverted(checkoutSession.getCart());
        return customerOrder;
    }
}
