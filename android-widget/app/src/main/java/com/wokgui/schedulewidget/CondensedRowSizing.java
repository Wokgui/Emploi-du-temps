package com.wokgui.schedulewidget;

/** Pure sizing rule shared by the condensed widget renderer and its JVM regression test. */
final class CondensedRowSizing {
    static final int COMPACT_ROW_DP = 17;
    static final int NATURAL_ROW_DP = 22;
    static final int COMFORTABLE_ROW_DP = 28;
    static final int PROGRESS_STRIP_DP = 3;
    static final int PROGRESS_CHROME_DP = PROGRESS_STRIP_DP * 2;
    static final int MANUAL_MIN_ROW_DP = 14;
    static final int MANUAL_MAX_ROW_DP = 30;
    static final int AUTO_MIN_ROW_DP = 14;

    private CondensedRowSizing() {}

    static int visibleRows(int widgetHeightDp, int itemCount) {
        return visibleRows(widgetHeightDp, itemCount, "normal");
    }

    static int visibleRows(int widgetHeightDp, int itemCount, String density) {
        int rowHeight = rowHeightForDensity(density);
        int contentHeight = Math.max(rowHeight, widgetHeightDp - PROGRESS_CHROME_DP);
        int naturalCapacity = Math.max(1, contentHeight / rowHeight);
        return Math.max(1, Math.min(Math.max(1, itemCount), naturalCapacity));
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position) {
        return rowHeightDp(widgetHeightDp, itemCount, position, "normal");
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position, String density) {
        return rowHeightForDensity(density);
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position,
                           int densityPercent, boolean automatic) {
        return automatic ? autoRowHeightDp(widgetHeightDp, itemCount, position)
                : rowHeightForPercent(densityPercent);
    }

    static int rowHeightForPercent(int densityPercent) {
        int percent = Math.max(0, Math.min(100, densityPercent));
        return Math.round(MANUAL_MIN_ROW_DP
                + (MANUAL_MAX_ROW_DP - MANUAL_MIN_ROW_DP) * (percent / 100f));
    }

    static int autoRowHeightDp(int widgetHeightDp, int itemCount) {
        return autoRowHeightDp(widgetHeightDp, itemCount, 0);
    }

    static int autoRowHeightDp(int widgetHeightDp, int itemCount, int position) {
        return autoRowHeightDp(widgetHeightDp, itemCount, position, PROGRESS_CHROME_DP);
    }

    static int autoRowHeightDp(int widgetHeightDp, int itemCount, int position, int progressChromeDp) {
        int count = Math.max(1, itemCount);
        int available = Math.max(AUTO_MIN_ROW_DP, widgetHeightDp - Math.max(0, progressChromeDp));
        int base = available / count;
        if (base < AUTO_MIN_ROW_DP) return AUTO_MIN_ROW_DP;
        int remainder = available % count;
        return base + (Math.max(0, position) < remainder ? 1 : 0);
    }

    static float textScaleForRow(int rowHeightDp) {
        return Math.max(0.34f, Math.min(1.20f, rowHeightDp / (float) NATURAL_ROW_DP));
    }

    static int rowHeightForDensity(String density) {
        if ("compact".equals(density)) return COMPACT_ROW_DP;
        if ("comfortable".equals(density)) return COMFORTABLE_ROW_DP;
        return NATURAL_ROW_DP;
    }
}
