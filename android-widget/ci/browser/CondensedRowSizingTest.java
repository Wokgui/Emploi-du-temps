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
        String[] densities = {"compact", "normal", "comfortable"};
        int[] expected = {
                CondensedRowSizing.COMPACT_ROW_DP,
                CondensedRowSizing.NATURAL_ROW_DP,
                CondensedRowSizing.COMFORTABLE_ROW_DP
        };
        for (int i = 0; i < densities.length; i++) {
            String density = densities[i];
            int row = CondensedRowSizing.rowHeightDp(108, 7, 0, density);
            if (row != expected[i]) throw new AssertionError("density not applied: " + density);
            int visible = CondensedRowSizing.visibleRows(108, 9, density);
            if (visible != Math.min(9, (108 - CondensedRowSizing.PROGRESS_STRIP_DP) / expected[i]))
                throw new AssertionError("density capacity is wrong: " + density);
        }
        if (CondensedRowSizing.visibleRows(108, 9, "compact")
                <= CondensedRowSizing.visibleRows(108, 9, "comfortable"))
            throw new AssertionError("compact mode must expose more rows");
        int previousContinuous = -1;
        int distinctHeights = 0;
        for (int percent = 0; percent <= 100; percent++) {
            int row = CondensedRowSizing.rowHeightForPercent(percent);
            if (row < previousContinuous) throw new AssertionError("continuous density went backwards");
            if (row != previousContinuous) distinctHeights++;
            previousContinuous = row;
        }
        if (distinctHeights < 10) throw new AssertionError("density is still a three-state selector");
        for (int height = 40; height <= 300; height++) {
            for (int count = 1; count <= 18; count++) {
                int row = CondensedRowSizing.autoRowHeightDp(height, count);
                int available = Math.max(CondensedRowSizing.AUTO_MIN_ROW_DP,
                        height - CondensedRowSizing.PROGRESS_STRIP_DP);
                if (row < CondensedRowSizing.AUTO_MIN_ROW_DP)
                    throw new AssertionError("automatic row became unreadably small");
                if (available / count >= CondensedRowSizing.AUTO_MIN_ROW_DP && row * count > available)
                    throw new AssertionError("automatic rows do not fit the widget");
            }
        }
        System.out.println("continuous_and_automatic_widget_density_666=passed");
        System.out.println("condensed_widget_density_665=passed");
    }
}
