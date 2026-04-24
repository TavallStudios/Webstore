package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentAdminInput {

    private String orderNumber;
    private String carrier;
    private String trackingNumber;
    private String status;
    private String trackingEventsJson;
}
