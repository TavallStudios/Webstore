package org.tavall.webstore.storefront.view;

public record AdminDashboardView(
        String storeName,
        long productCount,
        long activeEngagementCount,
        long orderCount,
        long mediaAssetCount,
        long pageCount,
        long sectionCount,
        long themeFileCount,
        long connectedPaymentProviderCount,
        String paymentMode,
        boolean storefrontThemeReady
) {
}
