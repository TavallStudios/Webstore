package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriptionPlanAdminInput {

    private Long id;
    private Long productId;
    private String name;
    private String intervalLabel;
    private int frequencyDays;
    private int discountPercent;
    private boolean active;
    private String configurationJson;
}
