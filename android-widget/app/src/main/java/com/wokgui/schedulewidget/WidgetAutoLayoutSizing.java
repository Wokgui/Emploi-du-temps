package com.wokgui.schedulewidget;

/** Pure adaptive measurements for the classic widget row. */
final class WidgetAutoLayoutSizing {
    private WidgetAutoLayoutSizing() {}

    static float classicTextScale(int rowHeightDp) {
        return Math.max(0.34f, Math.min(1f, Math.max(1, rowHeightDp) / 54f));
    }

    static float pillTextScale(int rowHeightDp) {
        return Math.max(0.72f, Math.min(1f, Math.max(1, rowHeightDp) / 32f));
    }

    static int pillHeightDp(int rowHeightDp) {
        int height = Math.max(1, rowHeightDp);
        return Math.max(1, Math.min(26, height - Math.min(4, Math.max(0, height - 1))));
    }

    static int pillWidthDp(int rowHeightDp) {
        return Math.max(30, Math.min(78, Math.round(pillHeightDp(rowHeightDp) * 2.70f)));
    }

    static int pillBoxWidthDp(int rowHeightDp) {
        return Math.min(86, pillWidthDp(rowHeightDp) + 6);
    }

    static boolean showMeta(int rowHeightDp) {
        return rowHeightDp >= 34;
    }

    static boolean showPill(int rowHeightDp) {
        return rowHeightDp >= 12;
    }
}
