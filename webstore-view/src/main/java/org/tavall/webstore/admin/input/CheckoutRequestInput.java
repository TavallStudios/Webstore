package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequestInput {

    private String provider;
    private String customerName;
    private String email;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String discountCode;
}
