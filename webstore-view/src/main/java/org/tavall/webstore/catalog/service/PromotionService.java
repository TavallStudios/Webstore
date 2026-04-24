package org.tavall.webstore.catalog.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tavall.webstore.catalog.model.Promotion;
import org.tavall.webstore.catalog.model.PromotionType;
import org.tavall.webstore.catalog.repository.PromotionRepository;

@Service
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public List<Promotion> listActivePromotions() {
        return promotionRepository.findAllByActiveTrueOrderByAutomaticDescDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(BigDecimal subtotal, String discountCode) {
        if (discountCode == null || discountCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        Promotion promotion = promotionRepository.findByOfferCodeIgnoreCaseAndActiveTrue(discountCode)
                .orElse(null);
        if (promotion == null) {
            return BigDecimal.ZERO;
        }

        return switch (promotion.getPromotionType()) {
            case PERCENTAGE, SUBSCRIPTION, BUNDLE ->
                    subtotal.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> promotion.getDiscountValue();
            default -> BigDecimal.ZERO;
        };
    }

    @Transactional(readOnly = true)
    public boolean qualifiesForFreeShipping(int quantity, BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(50)) >= 0) {
            return true;
        }

        return promotionRepository.findAllByActiveTrueOrderByAutomaticDescDisplayNameAsc().stream()
                .anyMatch(promotion -> promotion.isAutomatic()
                        && promotion.isFreeShipping()
                        && quantity >= ((Number) promotion.getConfiguration().getOrDefault("minimumQuantity", 0)).intValue());
    }
}
