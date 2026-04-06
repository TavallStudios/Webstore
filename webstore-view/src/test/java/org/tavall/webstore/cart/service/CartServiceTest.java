package org.tavall.webstore.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tavall.webstore.cart.model.ShoppingCart;
import org.tavall.webstore.cart.repository.CartLineItemRepository;
import org.tavall.webstore.cart.repository.ShoppingCartRepository;
import org.tavall.webstore.catalog.model.BundleOffer;
import org.tavall.webstore.catalog.model.Product;
import org.tavall.webstore.catalog.service.CatalogService;
import org.tavall.webstore.catalog.service.PromotionService;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartLineItemRepository cartLineItemRepository;

    @Mock
    private CatalogService catalogService;

    @Mock
    private PromotionService promotionService;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(shoppingCartRepository, cartLineItemRepository, catalogService, promotionService);
    }

    @Test
    void addProductToCartAppliesBundleQuantityAndCalculatesTotal() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setToken("cart-1");
        shoppingCart.setLineItems(new ArrayList<>());

        Product product = new Product();
        product.setId(10L);
        product.setName("Atlas Bottle");
        product.setSku("ATLAS");
        product.setPrice(BigDecimal.valueOf(40));

        BundleOffer bundleOffer = new BundleOffer();
        bundleOffer.setId(20L);
        bundleOffer.setName("Twin Pack");
        bundleOffer.setBundleQuantity(2);
        bundleOffer.setDiscountType("PERCENTAGE");
        bundleOffer.setDiscountValue(BigDecimal.valueOf(10));
        product.getBundleOffers().add(bundleOffer);

        when(shoppingCartRepository.findDetailedByTokenAndStatus("cart-1", org.tavall.webstore.cart.model.CartStatus.ACTIVE))
                .thenReturn(Optional.of(shoppingCart));
        when(catalogService.getProductForAdmin(10L)).thenReturn(product);
        when(catalogService.getBundleOffer(product, 20L)).thenReturn(bundleOffer);
        when(catalogService.getVariant(product, null)).thenReturn(null);
        when(catalogService.getSubscriptionPlan(product, null)).thenReturn(null);
        when(cartLineItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(shoppingCartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(promotionService.qualifiesForFreeShipping(eq(2), any(BigDecimal.class))).thenReturn(true);

        ShoppingCart updatedCart = cartService.addProductToCart("cart-1", 10L, null, 1, 20L, null);

        assertThat(updatedCart.getLineItems()).hasSize(1);
        assertThat(updatedCart.getLineItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(updatedCart.getSubtotalAmount()).isEqualByComparingTo("72.00");
        assertThat(updatedCart.getTotalAmount()).isEqualByComparingTo("72.00");
    }
}
