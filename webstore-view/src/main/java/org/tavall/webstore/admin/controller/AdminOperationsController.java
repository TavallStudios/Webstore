package org.tavall.webstore.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.admin.input.ShipmentAdminInput;
import org.tavall.webstore.content.service.FeatureFlagService;
import org.tavall.webstore.content.service.JsonContentService;
import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.Shipment;
import org.tavall.webstore.orders.service.OrderOperationsService;

@Controller
public class AdminOperationsController {

    private final FeatureFlagService featureFlagService;
    private final OrderOperationsService orderOperationsService;
    private final JsonContentService jsonContentService;

    public AdminOperationsController(
            FeatureFlagService featureFlagService,
            OrderOperationsService orderOperationsService,
            JsonContentService jsonContentService
    ) {
        this.featureFlagService = featureFlagService;
        this.orderOperationsService = orderOperationsService;
        this.jsonContentService = jsonContentService;
    }

    @GetMapping({"/admin/engagement", "/admin/feature-flags"})
    public String featureFlags(Model model) {
        model.addAttribute("featureFlags", featureFlagService.listFeatureFlags());
        return "admin/engagement";
    }

    @PostMapping({"/admin/engagement/{flagKey}", "/admin/feature-flags/{flagKey}"})
    public String updateFeatureFlag(@PathVariable String flagKey, @RequestParam(defaultValue = "false") boolean enabled) {
        featureFlagService.updateFeatureFlag(flagKey, enabled);
        return "redirect:/admin/engagement?saved=1";
    }

    @GetMapping("/admin/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderOperationsService.listOrders());
        return "admin/orders";
    }

    @GetMapping("/admin/orders/{orderNumber}")
    public String orderDetail(@PathVariable String orderNumber, Model model) {
        CustomerOrder customerOrder = orderOperationsService.getDetailedOrder(orderNumber);
        model.addAttribute("order", customerOrder);
        model.addAttribute("shipmentInput", toShipmentInput(customerOrder));
        return "admin/order-detail";
    }

    @PostMapping("/admin/orders/{orderNumber}/shipment")
    public String saveShipment(@PathVariable String orderNumber, @ModelAttribute ShipmentAdminInput shipmentAdminInput) {
        shipmentAdminInput.setOrderNumber(orderNumber);
        orderOperationsService.saveShipment(shipmentAdminInput);
        return "redirect:/admin/orders/" + orderNumber;
    }

    private ShipmentAdminInput toShipmentInput(CustomerOrder customerOrder) {
        Shipment shipment = customerOrder.getShipments().stream().findFirst().orElse(null);
        ShipmentAdminInput shipmentAdminInput = new ShipmentAdminInput();
        shipmentAdminInput.setOrderNumber(customerOrder.getOrderNumber());
        shipmentAdminInput.setCarrier(shipment == null ? "" : shipment.getCarrier());
        shipmentAdminInput.setTrackingNumber(shipment == null ? "" : shipment.getTrackingNumber());
        shipmentAdminInput.setStatus(shipment == null ? "PENDING" : shipment.getStatus().name());
        shipmentAdminInput.setTrackingEventsJson(jsonContentService.writeJson(
                shipment == null ? java.util.List.of() : shipment.getTrackingEvents().stream().map(event -> java.util.Map.of(
                        "eventTimestamp", event.getEventTimestamp().toString(),
                        "status", event.getStatus(),
                        "location", event.getLocation(),
                        "message", event.getMessage()
                )).toList()
        ));
        return shipmentAdminInput;
    }
}
