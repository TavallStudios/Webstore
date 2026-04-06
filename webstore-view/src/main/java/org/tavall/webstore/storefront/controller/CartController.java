package org.tavall.webstore.storefront.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tavall.webstore.cart.service.CartService;
import org.tavall.webstore.cart.service.CartSessionService;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private final CartService cartService;
    private final CartSessionService cartSessionService;

    public CartController(CartService cartService, CartSessionService cartSessionService) {
        this.cartService = cartService;
        this.cartSessionService = cartSessionService;
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        model.addAttribute("cart", cartService.loadOrCreateActiveCart(cartToken));
        return "storefront/cart";
    }

    @PostMapping("/cart/items")
    public String addProductToCart(
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Long bundleOfferId,
            @RequestParam(required = false) Long subscriptionPlanId,
            HttpSession session
    ) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        cartService.addProductToCart(cartToken, productId, variantId, quantity, bundleOfferId, subscriptionPlanId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{lineItemId}")
    public String updateCartLine(@PathVariable Long lineItemId, @RequestParam int quantity, HttpSession session) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        cartService.updateCartLineQuantity(cartToken, lineItemId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/items/{lineItemId}/remove")
    public String removeCartLine(@PathVariable Long lineItemId, HttpSession session) {
        String cartToken = cartSessionService.getOrCreateCartToken(session);
        cartService.removeCartLine(cartToken, lineItemId);
        return "redirect:/cart";
    }
}
