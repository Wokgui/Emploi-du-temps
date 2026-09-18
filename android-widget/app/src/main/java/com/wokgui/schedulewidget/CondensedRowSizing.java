package com.wokgui.schedulewidget;

/** Pure sizing rule shared by the condensed widget renderer and its JVM regression test. */
final class CondensedRowSizing {
    static final int COMPACT_ROW_DP = 17;
    static final int NATURAL_ROW_DP = 22;
    static final int COMFORTABLE_ROW_DP = 28;
    static final int PROGRESS_STRIP_DP = 3;

    private CondensedRowSizing() {}

    static int visibleRows(int widgetHeightDp, int itemCount) {
        return visibleRows(widgetHeightDp, itemCount, "normal");
    }

    static int visibleRows(int widgetHeightDp, int itemCount, String density) {
        int rowHeight = rowHeightForDensity(density);
        int contentHeight = Math.max(rowHeight, widgetHeightDp - PROGRESS_STRIP_DP);
        int naturalCapacity = Math.max(1, contentHeight / rowHeight);
        return Math.max(1, Math.min(Math.max(1, itemCount), naturalCapacity));
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position) {
        return rowHeightDp(widgetHeightDp, itemCount, position, "normal");
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position, String density) {
        return rowHeightForDensity(density);
    }

    static int rowHeightForDensity(String density) {
        if ("compact".equals(density)) return COMPACT_ROW_DP;
        if ("comfortable".equals(density)) return COMFORTABLE_ROW_DP;
        return NATURAL_ROW_DP;
    }
}
