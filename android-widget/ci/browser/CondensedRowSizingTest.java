package com.wokgui.schedulewidget;

/** JVM proof that the condensed format keeps every row at its dense readable height. */
public final class CondensedRowSizingTest {
    public static void main(String[] args) {
        for (int widgetHeight = 40; widgetHeight <= 240; widgetHeight++) {
            for (int itemCount = 1; itemCount <= 9; itemCount++) {
                int visible = CondensedRowSizing.visibleRows(widgetHeight, itemCount);
                int used = 0;
                for (int position = 0; position < visible; position++) {
                    int row = CondensedRowSizing.rowHeightDp(widgetHeight, itemCount, position);
                    if (row != CondensedRowSizing.NATURAL_ROW_DP) throw new AssertionError("row is not dense");
                    used += row;
                }
                int available = Math.max(CondensedRowSizing.NATURAL_ROW_DP,
                        widgetHeight - CondensedRowSizing.PROGRESS_STRIP_DP);
                if (used > available) throw new AssertionError("visible rows overflow the widget");
                if (itemCount > visible && available - used >= CondensedRowSizing.NATURAL_ROW_DP)
                    throw new AssertionError("another dense row should be visible");
            }
        }
        System.out.println("condensed_widget_dense_rows_662=passed");
    }
}
