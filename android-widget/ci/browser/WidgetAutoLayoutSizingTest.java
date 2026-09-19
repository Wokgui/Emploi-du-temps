package com.wokgui.schedulewidget;

/** JVM regression checks for adaptive classic-widget rows and bubbles. */
public final class WidgetAutoLayoutSizingTest {
    public static void main(String[] args) {
        int previousHeight = 0;
        int previousWidth = 0;
        for (int row = 1; row <= 80; row++) {
            int pillHeight = WidgetAutoLayoutSizing.pillHeightDp(row);
            int pillWidth = WidgetAutoLayoutSizing.pillWidthDp(row);
            int boxWidth = WidgetAutoLayoutSizing.pillBoxWidthDp(row);
            if (pillHeight > row) throw new AssertionError("pill exceeds row at " + row);
            if (pillHeight < previousHeight) throw new AssertionError("pill height went backwards");
            if (pillWidth < previousWidth) throw new AssertionError("pill width went backwards");
            if (boxWidth < pillWidth || boxWidth > 86) throw new AssertionError("invalid pill box width");
            float scale = WidgetAutoLayoutSizing.classicTextScale(row);
            float pillScale = WidgetAutoLayoutSizing.pillTextScale(row);
            if (scale < 0.34f || scale > 1f) throw new AssertionError("invalid text scale");
            if (pillScale < 0.72f || pillScale > 1f) throw new AssertionError("invalid pill text scale");
            previousHeight = pillHeight;
            previousWidth = pillWidth;
        }
        if (WidgetAutoLayoutSizing.pillHeightDp(22) >= WidgetAutoLayoutSizing.pillHeightDp(54))
            throw new AssertionError("small rows did not shrink the pill");
        if (WidgetAutoLayoutSizing.pillWidthDp(22) >= WidgetAutoLayoutSizing.pillWidthDp(54))
            throw new AssertionError("small rows did not narrow the pill");
        if (WidgetAutoLayoutSizing.pillHeightDp(80) != 26) throw new AssertionError("pill height cap changed");
        if (WidgetAutoLayoutSizing.pillWidthDp(80) > 78) throw new AssertionError("pill width cap changed");
        if (WidgetAutoLayoutSizing.showMeta(20)) throw new AssertionError("metadata must hide in tiny rows");
        if (WidgetAutoLayoutSizing.showPill(10)) throw new AssertionError("pill must hide before it can fit");
        if (!WidgetAutoLayoutSizing.showPill(22)) throw new AssertionError("pill should fit in a normal compact row");
        System.out.println("adaptive_classic_widget_bubbles_668=passed");
    }
}
