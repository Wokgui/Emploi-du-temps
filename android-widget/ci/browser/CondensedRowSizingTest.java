package com.wokgui.schedulewidget;

/** JVM proof that the condensed rows consume every available dp above the progress strip. */
public final class CondensedRowSizingTest {
    public static void main(String[] args) {
        for (int widgetHeight = 40; widgetHeight <= 240; widgetHeight++) {
            for (int itemCount = 1; itemCount <= 9; itemCount++) {
                int visible = CondensedRowSizing.visibleRows(widgetHeight, itemCount);
                int filled = CondensedRowSizing.PROGRESS_STRIP_DP;
                for (int position = 0; position < visible; position++) {
                    int row = CondensedRowSizing.rowHeightDp(widgetHeight, itemCount, position);
                    if (row < CondensedRowSizing.NATURAL_ROW_DP) {
                        throw new AssertionError("row below readable minimum");
                    }
                    filled += row;
                }
                int expected = Math.max(
                        CondensedRowSizing.NATURAL_ROW_DP,
                        widgetHeight - CondensedRowSizing.PROGRESS_STRIP_DP
                ) + CondensedRowSizing.PROGRESS_STRIP_DP;
                if (filled != expected) {
                    throw new AssertionError("unfilled height widget=" + widgetHeight
                            + " items=" + itemCount + " filled=" + filled + " expected=" + expected);
                }
            }
        }
        System.out.println("condensed_widget_full_height=passed");
    }
}
