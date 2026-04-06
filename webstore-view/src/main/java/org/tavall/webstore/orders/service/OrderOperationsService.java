package org.tavall.webstore.orders.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.tavall.webstore.admin.input.ShipmentAdminInput;
import org.tavall.webstore.cart.model.CartLineItem;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.model.CheckoutSessionStatus;
import org.tavall.webstore.content.service.JsonContentService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.FulfillmentStatus;
import org.tavall.webstore.orders.model.OrderLineItem;
import org.tavall.webstore.orders.model.OrderStatus;
import org.tavall.webstore.orders.model.PaymentStatus;
import org.tavall.webstore.orders.model.Shipment;
import org.tavall.webstore.orders.model.ShipmentStatus;
import org.tavall.webstore.orders.model.TrackingEvent;
import org.tavall.webstore.orders.repository.CustomerOrderRepository;
import org.tavall.webstore.orders.repository.ShipmentRepository;

@Service
public class OrderOperationsService {

    private final CustomerOrderRepository customerOrderRepository;
    private final ShipmentRepository shipmentRepository;
    private final JsonContentService jsonContentService;

    public OrderOperationsService(
            CustomerOrderRepository customerOrderRepository,
            ShipmentRepository shipmentRepository,
            JsonContentService jsonContentService
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.shipmentRepository = shipmentRepository;
        this.jsonContentService = jsonContentService;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> listOrders() {
        return customerOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public CustomerOrder getDetailedOrder(String orderNumber) {
        CustomerOrder customerOrder = customerOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Unknown order number: " + orderNumber));
        Hibernate.initialize(customerOrder.getLineItems());
        Hibernate.initialize(customerOrder.getShipments());
        customerOrder.getShipments().forEach(shipment -> Hibernate.initialize(shipment.getTrackingEvents()));
        return customerOrder;
    }

    @Transactional(readOnly = true)
    public CustomerOrder findTrackableOrder(String orderNumber, String email) {
        CustomerOrder customerOrder = getDetailedOrder(orderNumber);
        if (email != null && !email.isBlank() && !customerOrder.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Order email does not match.");
        }
        return customerOrder;
    }

    @Transactional
    public CustomerOrder createOrderFromCheckoutSession(CheckoutSession checkoutSession) {
        CustomerOrder existingOrder = customerOrderRepository.findByCheckoutSessionId(checkoutSession.getId()).orElse(null);
        if (existingOrder != null) {
            return existingOrder;
        }

        Map<String, Object> checkoutData = checkoutSession.getCheckoutData();
        CustomerOrder customerOrder = new CustomerOrder();
        customerOrder.setOrderNumber("PENDING-" + checkoutSession.getExternalReference());
        customerOrder.setCheckoutSession(checkoutSession);
        customerOrder.setStatus(checkoutSession.getStatus() == CheckoutSessionStatus.COMPLETED ? OrderStatus.PAID : OrderStatus.CONFIRMED);
        customerOrder.setPaymentStatus(checkoutSession.getStatus() == CheckoutSessionStatus.COMPLETED ? PaymentStatus.PAID : PaymentStatus.PENDING);
        customerOrder.setFulfillmentStatus(FulfillmentStatus.UNFULFILLED);
        customerOrder.setEmail(String.valueOf(checkoutData.getOrDefault("email", "customer@example.test")));
        customerOrder.setCustomerName(String.valueOf(checkoutData.getOrDefault("customerName", "Storefront Customer")));
        customerOrder.setCurrency(checkoutSession.getCurrency());
        customerOrder.setSubtotalAmount(checkoutSession.getCart().getSubtotalAmount());
        customerOrder.setDiscountAmount((java.math.BigDecimal) checkoutData.getOrDefault("discountAmount", java.math.BigDecimal.ZERO));
        customerOrder.setShippingAmount((java.math.BigDecimal) checkoutData.getOrDefault("shippingAmount", checkoutSession.getCart().getShippingAmount()));
        customerOrder.setTaxAmount((java.math.BigDecimal) checkoutData.getOrDefault("taxAmount", java.math.BigDecimal.ZERO));
        customerOrder.setTotalAmount(checkoutSession.getAmount());
        customerOrder.setDiscountCode((String) checkoutData.get("discountCode"));
        customerOrder.setShippingAddress(extractAddress(checkoutData));
        customerOrder.setBillingAddress(extractAddress(checkoutData));
        customerOrder.setOrderAttributes(new HashMap<>(Map.of("provider", checkoutSession.getProvider().name())));
        CustomerOrder savedOrder = customerOrderRepository.save(customerOrder);
        savedOrder.setOrderNumber("WS-" + String.format("%06d", savedOrder.getId() + 100000));

        for (CartLineItem cartLineItem : checkoutSession.getCart().getLineItems()) {
            OrderLineItem orderLineItem = new OrderLineItem();
            orderLineItem.setCustomerOrder(savedOrder);
            orderLineItem.setProduct(cartLineItem.getProduct());
            orderLineItem.setVariant(cartLineItem.getVariant());
            orderLineItem.setProductName(cartLineItem.getProductNameSnapshot());
            orderLineItem.setSku(cartLineItem.getSkuSnapshot());
            orderLineItem.setQuantity(cartLineItem.getQuantity());
            orderLineItem.setUnitPrice(cartLineItem.getUnitPrice());
            orderLineItem.setLineTotal(cartLineItem.getLineTotal());
            orderLineItem.setLineAttributes(new HashMap<>(cartLineItem.getLineAttributes()));
            savedOrder.getLineItems().add(orderLineItem);
        }

        return customerOrderRepository.save(savedOrder);
    }

    @Transactional
    public Shipment saveShipment(ShipmentAdminInput input) {
        CustomerOrder customerOrder = getDetailedOrder(input.getOrderNumber());
        Shipment shipment = customerOrder.getShipments().stream().findFirst().orElseGet(Shipment::new);
        shipment.setCustomerOrder(customerOrder);
        shipment.setCarrier(input.getCarrier());
        shipment.setTrackingNumber(input.getTrackingNumber());
        ShipmentStatus shipmentStatus = ShipmentStatus.valueOf(input.getStatus());
        shipment.setStatus(shipmentStatus);
        shipment.setTrackingPayload(new HashMap<>(Map.of("updatedFromAdmin", true)));
        if (shipmentStatus == ShipmentStatus.SHIPPED || shipmentStatus == ShipmentStatus.IN_TRANSIT || shipmentStatus == ShipmentStatus.DELIVERED) {
            shipment.setShippedAt(shipment.getShippedAt() == null ? Instant.now() : shipment.getShippedAt());
        }
        if (shipmentStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(Instant.now());
            customerOrder.setFulfillmentStatus(FulfillmentStatus.DELIVERED);
        } else if (shipmentStatus == ShipmentStatus.IN_TRANSIT || shipmentStatus == ShipmentStatus.SHIPPED) {
            customerOrder.setFulfillmentStatus(FulfillmentStatus.SHIPPED);
        }

        shipment.getTrackingEvents().clear();
        for (Map<String, Object> eventMap : jsonContentService.parseObjectList(input.getTrackingEventsJson())) {
            TrackingEvent trackingEvent = new TrackingEvent();
            trackingEvent.setShipment(shipment);
            trackingEvent.setEventTimestamp(Instant.parse(String.valueOf(eventMap.getOrDefault("eventTimestamp", Instant.now().toString()))));
            trackingEvent.setStatus(String.valueOf(eventMap.getOrDefault("status", shipmentStatus.name())));
            trackingEvent.setLocation((String) eventMap.get("location"));
            trackingEvent.setMessage(String.valueOf(eventMap.getOrDefault("message", "Shipment update recorded.")));
            trackingEvent.setEventPayload(new HashMap<>(eventMap));
            shipment.getTrackingEvents().add(trackingEvent);
        }

        customerOrderRepository.save(customerOrder);
        return shipmentRepository.save(shipment);
    }

    private Map<String, Object> extractAddress(Map<String, Object> checkoutData) {
        Map<String, Object> address = new HashMap<>();
        address.put("addressLine1", checkoutData.get("addressLine1"));
        address.put("addressLine2", checkoutData.get("addressLine2"));
        address.put("city", checkoutData.get("city"));
        address.put("state", checkoutData.get("state"));
        address.put("postalCode", checkoutData.get("postalCode"));
        address.put("country", checkoutData.get("country"));
        return address;
    }
}
