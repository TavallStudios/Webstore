package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSettingsAdminInput {

    private String mode;
    private String defaultProvider;
    private String sandboxNotice;
    private boolean stripeEnabled;
    private String stripePublishableKey;
    private String stripeSecretKey;
    private String stripeWebhookSecret;
    private String stripeAccountId;
    private String stripeStatementDescriptor;
    private boolean paypalEnabled;
    private String paypalClientId;
    private String paypalSecret;
    private String paypalWebhookId;
    private String paypalMerchantId;
    private String paypalStatementDescriptor;
    private boolean manualReviewEnabled;
    private boolean saveCardsEnabled;
}
