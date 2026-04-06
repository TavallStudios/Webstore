package org.tavall.webstore.catalog.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.tavall.webstore.catalog.model.Product;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void savesAndLoadsJsonBackedProductFields() {
        Product product = new Product();
        product.setSlug("test-bottle");
        product.setName("Test Bottle");
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setCurrency("USD");
        product.setSku("TEST-BOTTLE-001");
        product.setMediaAssets(List.of(Map.of("path", "/media/test.png", "alt", "Bottle")));
        product.setFaqEntries(List.of(Map.of("question", "Q?", "answer", "A!")));

        productRepository.saveAndFlush(product);

        Product persistedProduct = productRepository.findBySlugAndActiveTrue("test-bottle").orElseThrow();
        assertThat(persistedProduct.getMediaAssets()).hasSize(1);
        assertThat(persistedProduct.getFaqEntries()).extracting(entry -> entry.get("question")).containsExactly("Q?");
    }
}
