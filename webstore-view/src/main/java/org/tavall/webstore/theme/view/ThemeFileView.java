package org.tavall.webstore.theme.view;

import java.time.Instant;

public record ThemeFileView(
        String name,
        String extension,
        long size,
        Instant lastModified,
        boolean runtimeFile,
        String publicPath
) {
}
