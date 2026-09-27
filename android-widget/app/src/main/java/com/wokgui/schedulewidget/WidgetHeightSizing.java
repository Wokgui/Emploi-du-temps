package com.wokgui.schedulewidget;

/** Chooses the launcher-provided height for the current orientation and removes fixed chrome. */
final class WidgetHeightSizing {
    private WidgetHeightSizing() {}

    static int resolveHeightDp(int minHeightDp, int maxHeightDp, boolean landscape, int fallbackDp) {
        int preferred = landscape ? minHeightDp : maxHeightDp;
        int alternate = landscape ? maxHeightDp : minHeightDp;
        if (preferred > 0) return preferred;
        if (alternate > 0) return alternate;
        return Math.max(1, fallbackDp);
    }

    /** Conservative estimate used only to size text inside rows whose real height is weight-driven. */
    static int smallestHeightDp(int minHeightDp, int maxHeightDp, int fallbackDp) {
        if (minHeightDp > 0 && maxHeightDp > 0) return Math.min(minHeightDp, maxHeightDp);
        if (minHeightDp > 0) return minHeightDp;
        if (maxHeightDp > 0) return maxHeightDp;
        return Math.max(1, fallbackDp);
    }

    static int adaptiveEstimateHeightDp(int launcherHeightDp) {
        return Math.max(1, launcherHeightDp - 8);
    }
    static int contentHeightDp(int minHeightDp, int maxHeightDp, boolean landscape,
                               int fallbackDp, int fixedChromeDp, int minimumContentDp) {
        int height = resolveHeightDp(minHeightDp, maxHeightDp, landscape, fallbackDp);
        return Math.max(minimumContentDp, height - Math.max(0, fixedChromeDp));
    }
}
