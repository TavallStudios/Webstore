package org.tavall.webstore.storefront.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.orders.service.OrderOperationsService;
import org.tavall.webstore.storefront.view.HomePageView;
import org.tavall.webstore.storefront.view.ProductPageView;
import org.tavall.webstore.storefront.view.StorefrontQueryService;
import org.tavall.webstore.storefront.view.TrackingView;

@Controller
public class StorefrontController {

    private final StorefrontQueryService storefrontQueryService;
    private final OrderOperationsService orderOperationsService;

    public StorefrontController(StorefrontQueryService storefrontQueryService, OrderOperationsService orderOperationsService) {
        this.storefrontQueryService = storefrontQueryService;
        this.orderOperationsService = orderOperationsService;
    }

    @GetMapping("/")
    public String homepage(Model model) {
        HomePageView homePageView = storefrontQueryService.buildHomepageView();
        model.addAttribute("view", homePageView);
        return "storefront/home";
    }

    @GetMapping("/products/{slug}")
    public String productPage(@PathVariable String slug, Model model) {
        ProductPageView productPageView = storefrontQueryService.buildProductPageView(slug);
        model.addAttribute("view", productPageView);
        return "storefront/product";
    }

    @GetMapping("/tracking")
    public String trackingLookup() {
        return "storefront/tracking-lookup";
    }

    @PostMapping("/tracking")
    public String trackingResults(@RequestParam String orderNumber, @RequestParam(required = false) String email, Model model) {
        TrackingView trackingView = storefrontQueryService.buildTrackingView(orderNumber, email);
        model.addAttribute("view", trackingView);
        return "storefront/tracking-detail";
    }

    @GetMapping("/orders/{orderNumber}/confirmation")
    public String orderConfirmation(@PathVariable String orderNumber, Model model) {
        model.addAttribute("order", orderOperationsService.getDetailedOrder(orderNumber));
        return "storefront/order-confirmation";
    }
}
