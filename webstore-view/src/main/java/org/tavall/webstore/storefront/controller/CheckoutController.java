package org.tavall.webstore.storefront.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.admin.input.CheckoutRequestInput;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.cart.service.CartSessionService;
import org.tavall.webstore.checkout.model.CheckoutSession;
import org.tavall.webstore.checkout.service.CheckoutService;
import org.tavall.webstore.orders.model.CustomerOrder;
import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final CartSessionService cartSessionService;
    private final CheckoutService checkoutService;

    public CheckoutController(
            CartService cartService,
            CartSessionService cartSessionService,
            CheckoutService checkoutService
    ) {
        this.cartService = cartService;
        this.cartSessionService = cartSessionService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        model.addAttribute("cart", cartService.loadOrCreateActiveCart(cartToken));
        return "storefront/checkout";
    }

    @PostMapping("/checkout/sessions")
    public String createCheckoutSession(CheckoutRequestInput input, HttpSession session) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        CheckoutSession checkoutSession = checkoutService.createCheckoutSession(cartToken, input);
        return "redirect:/checkout/sessions/" + checkoutSession.getExternalReference();
    }

    @GetMapping("/checkout/sessions/{externalReference}")
    public String checkoutSession(@PathVariable String externalReference, Model model) {
        model.addAttribute("checkoutSession", checkoutService.getCheckoutSession(externalReference));
        return "storefront/checkout-session";
    }

    @PostMapping("/checkout/sessions/{externalReference}/simulate-success")
    public String simulateSuccess(@PathVariable String externalReference) {
        CustomerOrder customerOrder = checkoutService.simulateSuccessfulCheckout(externalReference);
        return "redirect:/orders/" + customerOrder.getOrderNumber() + "/confirmation";
    }
}
