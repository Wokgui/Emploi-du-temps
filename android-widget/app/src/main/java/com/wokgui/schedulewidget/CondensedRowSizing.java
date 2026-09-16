package com.wokgui.schedulewidget;

/** Pure sizing rule shared by the condensed widget renderer and its JVM regression test. */
final class CondensedRowSizing {
    static final int NATURAL_ROW_DP = 22;
    static final int PROGRESS_STRIP_DP = 3;

    private CondensedRowSizing() {}

    static int visibleRows(int widgetHeightDp, int itemCount) {
        int contentHeight = Math.max(NATURAL_ROW_DP, widgetHeightDp - PROGRESS_STRIP_DP);
        int naturalCapacity = Math.max(1, contentHeight / NATURAL_ROW_DP);
        return Math.max(1, Math.min(Math.max(1, itemCount), naturalCapacity));
    }

    static int rowHeightDp(int widgetHeightDp, int itemCount, int position) {
        return NATURAL_ROW_DP;
    }
}
