package com.wokgui.schedulewidget;

/** Pure adaptive measurements for the classic widget row. */
final class WidgetAutoLayoutSizing {
    private WidgetAutoLayoutSizing() {}

    static float classicTextScale(int rowHeightDp) {
        return Math.max(0.34f, Math.min(1f, Math.max(1, rowHeightDp) / 54f));
    }

    static int pillHeightDp(int rowHeightDp) {
        int height = Math.max(1, rowHeightDp);
        return Math.max(1, Math.min(44, height - Math.min(4, Math.max(0, height - 1))));
    }

    static int pillWidthDp(int rowHeightDp) {
        return Math.max(24, Math.min(100, Math.round(pillHeightDp(rowHeightDp) * 2.30f)));
    }

    static int pillBoxWidthDp(int rowHeightDp) {
        return Math.min(118, pillWidthDp(rowHeightDp) + 10);
    }

    static boolean showMeta(int rowHeightDp) {
        return rowHeightDp >= 34;
    }

    static boolean showPill(int rowHeightDp) {
        return rowHeightDp >= 14;
    }
}
