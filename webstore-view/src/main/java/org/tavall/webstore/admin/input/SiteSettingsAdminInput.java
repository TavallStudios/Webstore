package org.tavall.webstore.admin.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteSettingsAdminInput {

    private String siteName;
    private String logoPath;
    private String faviconPath;
    private String supportEmail;
    private String headerCtaText;
    private String shippingMessage;
    private String returnMessage;
    private String guaranteeMessage;
    private String brandPaletteJson;
    private String typographyJson;
    private String socialLinksJson;
    private String seoDefaultsJson;
    private String analyticsSettingsJson;
    private String paymentSettingsJson;
    private String reviewSourceJson;
    private String footerContentJson;
    private String announcementBarsJson;
    private String promoBannersJson;
    private String trustBadgesJson;
}
