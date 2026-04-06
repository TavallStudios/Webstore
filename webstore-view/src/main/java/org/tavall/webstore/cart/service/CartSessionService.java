package org.tavall.webstore.cart.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;

@Service
public class CartSessionService {

    public static final String CART_TOKEN_SESSION_KEY = "cartToken";

    public String getOrCreateCartToken(HttpSession session) {
        Object existingToken = session.getAttribute(CART_TOKEN_SESSION_KEY);
        if (existingToken instanceof String token && !token.isBlank()) {
            return token;
        }

        String newToken = "cart-" + UUID.randomUUID();
        session.setAttribute(CART_TOKEN_SESSION_KEY, newToken);
        return newToken;
    }

    public String getExistingCartToken(HttpSession session) {
        Object existingToken = session.getAttribute(CART_TOKEN_SESSION_KEY);
        return existingToken instanceof String token ? token : null;
    }
}
