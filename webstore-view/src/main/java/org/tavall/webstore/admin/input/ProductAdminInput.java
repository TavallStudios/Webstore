package org.tavall.webstore.admin.input;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAdminInput {

    private Long id;
    private String slug;
    private String name;
    private String tagline;
    private String shortDescription;
    private String fullDescription;
    private boolean active;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private String sku;
    private String barcode;
    private boolean inventoryTracking;
    private int inventoryQuantity;
    private boolean subscriptionAvailable;
    private boolean bundleEligible;
    private String shippingProfile;
    private String taxCategory;
    private String mediaAssetsJson;
    private String badgesJson;
    private String highlightsJson;
    private String benefitsJson;
    private String faqEntriesJson;
    private String guaranteeText;
    private String reviewSummaryJson;
    private String seoMetadataJson;
    private String customAttributesJson;
    private String presentationMetadataJson;
}
