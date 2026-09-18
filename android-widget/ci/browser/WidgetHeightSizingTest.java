package com.wokgui.schedulewidget;

/** JVM checks for orientation-aware widget height and the denser one-line rows. */
public final class WidgetHeightSizingTest {
    public static void main(String[] args) {
        equal(102, WidgetHeightSizing.resolveHeightDp(51, 102, false, 40), "portrait height");
        equal(51, WidgetHeightSizing.resolveHeightDp(51, 102, true, 40), "landscape height");
        equal(77, WidgetHeightSizing.resolveHeightDp(0, 77, true, 40), "missing preferred height");
        equal(40, WidgetHeightSizing.resolveHeightDp(0, 0, false, 40), "fallback height");
        equal(78, WidgetHeightSizing.adaptiveEstimateHeightDp(102), "launcher-safe estimate");
        equal(99, WidgetHeightSizing.contentHeightDp(51, 102, false, 40, 3, 28), "portrait content");
        equal(72, WidgetHeightSizing.contentHeightDp(40, 40, false, 40, 3, 72), "mini minimum");
        equal(22, CondensedRowSizing.NATURAL_ROW_DP, "compact row");
        equal(3, CondensedRowSizing.PROGRESS_STRIP_DP, "progress strip");
        System.out.println("widget_height_and_compact_rows_662=passed");
    }

    private static void equal(int expected, int actual, String label) {
        if (expected != actual) throw new AssertionError(label + ": " + actual + " != " + expected);
    }
}
