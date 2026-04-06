package org.tavall.webstore.storefront.view;

import java.time.Instant;

public record MediaAssetDescriptor(
        String fileName,
        String publicPath,
        long size,
        Instant lastModified
) {
}
