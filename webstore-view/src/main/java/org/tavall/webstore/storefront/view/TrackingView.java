package org.tavall.webstore.storefront.view;

import org.tavall.webstore.orders.model.CustomerOrder;
import org.tavall.webstore.orders.model.Shipment;

public record TrackingView(
        CustomerOrder order,
        Shipment shipment
) {
}
