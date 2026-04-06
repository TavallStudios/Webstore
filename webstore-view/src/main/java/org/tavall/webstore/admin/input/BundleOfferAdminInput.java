package org.tavall.webstore.admin.input;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleOfferAdminInput {

    private Long id;
    private Long productId;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private int bundleQuantity;
    private boolean active;
    private String placement;
    private String configurationJson;
}
