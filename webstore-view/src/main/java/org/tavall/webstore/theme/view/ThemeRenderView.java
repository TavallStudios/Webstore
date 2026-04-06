package org.tavall.webstore.theme.view;

public record ThemeRenderView(
        boolean cssAvailable,
        boolean scriptAvailable,
        String headHtml,
        String bodyTopHtml,
        String bodyBottomHtml,
        String homeTopHtml,
        String homeBottomHtml,
        String productTopHtml,
        String productBottomHtml,
        String footerHtml
) {
    public static ThemeRenderView empty() {
        return new ThemeRenderView(false, false, "", "", "", "", "", "", "", "");
    }
}
