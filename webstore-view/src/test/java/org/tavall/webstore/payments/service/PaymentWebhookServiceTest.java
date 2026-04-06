package org.tavall.webstore.payments.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tavall.webstore.cart.model.ShoppingCart;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.checkout.model.CheckoutProvider;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.repository.CheckoutSessionRepository;
import org.tavall.webstore.checkout.repository.WebhookEventLogRepository;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.repository.CustomerOrderRepository;
import org.tavall.webstore.orders.repository.PaymentRecordRepository;
import org.tavall.webstore.orders.service.OrderOperationsService;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private WebhookEventLogRepository webhookEventLogRepository;

    @Mock
    private CheckoutSessionRepository checkoutSessionRepository;

    @Mock
    private OrderOperationsService orderOperationsService;

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private PaymentRecordRepository paymentRecordRepository;

    @Mock
    private CartService cartService;

    private PaymentWebhookService paymentWebhookService;

    @BeforeEach
    void setUp() {
        paymentWebhookService = new PaymentWebhookService(
                webhookEventLogRepository,
                checkoutSessionRepository,
                orderOperationsService,
                customerOrderRepository,
                paymentRecordRepository,
                cartService
        );
    }

    @Test
    void duplicateWebhookReturnsExistingOrderWithoutCreatingAnotherPaymentRecord() {
        CheckoutSession checkoutSession = new CheckoutSession();
        checkoutSession.setId(1L);
        checkoutSession.setExternalReference("chk_1");
        checkoutSession.setCart(new ShoppingCart());

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100001");

        when(checkoutSessionRepository.findDetailedByExternalReference("chk_1")).thenReturn(Optional.of(checkoutSession));
        when(webhookEventLogRepository.existsByProviderAndEventId(CheckoutProvider.STRIPE, "evt_1")).thenReturn(true);
        when(customerOrderRepository.findByCheckoutSessionId(1L)).thenReturn(Optional.of(customerOrder));

        CustomerOrder result = paymentWebhookService.processPaymentEvent(
                CheckoutProvider.STRIPE,
                "evt_1",
                "chk_1",
                "pi_1",
                Map.of("status", "paid")
        );

        assertThat(result.getOrderNumber()).isEqualTo("WS-100001");
        verify(paymentRecordRepository, never()).save(any());
        verify(cartService, never()).markConverted(any());
    }

    @Test
    void newWebhookCreatesPaymentRecordAndMarksCartConverted() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setLineItems(new ArrayList<>());

        CheckoutSession checkoutSession = new CheckoutSession();
        checkoutSession.setId(2L);
        checkoutSession.setExternalReference("chk_2");
        checkoutSession.setProvider(CheckoutProvider.PAYPAL);
        checkoutSession.setCurrency("USD");
        checkoutSession.setAmount(BigDecimal.valueOf(75));
        checkoutSession.setCart(shoppingCart);

        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("WS-100002");

        when(checkoutSessionRepository.findDetailedByExternalReference("chk_2")).thenReturn(Optional.of(checkoutSession));
        when(webhookEventLogRepository.existsByProviderAndEventId(CheckoutProvider.PAYPAL, "evt_2")).thenReturn(false);
        when(orderOperationsService.createOrderFromCheckoutSession(checkoutSession)).thenReturn(customerOrder);
        when(customerOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = paymentWebhookService.processPaymentEvent(
                CheckoutProvider.PAYPAL,
                "evt_2",
                "chk_2",
                "pay_2",
                Map.of("status", "paid")
        );

        assertThat(result.getOrderNumber()).isEqualTo("WS-100002");
        verify(paymentRecordRepository).save(any());
        verify(cartService).markConverted(shoppingCart);
    }
}
