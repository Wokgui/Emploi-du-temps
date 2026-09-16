package com.wokgui.schedulewidget;

import java.util.List;

/** JVM coverage for the break sequence shared by both list widgets. */
public final class WidgetBreakSequenceTest {
    public static void main(String[] args) {
        List<WidgetBreakSequence.Segment> all = WidgetBreakSequence.between(
                11 * 60, 14 * 60, 12 * 60, 13 * 60, true, true, -1);
        require(all.size() == 3, "gap/lunch/gap sequence missing");
        require(all.get(0).type == WidgetBreakSequence.GAP && all.get(0).start == 660 && all.get(0).end == 720,
                "morning gap invalid");
        require(all.get(1).type == WidgetBreakSequence.LUNCH && all.get(1).start == 720 && all.get(1).end == 780,
                "lunch invalid");
        require(all.get(2).type == WidgetBreakSequence.GAP && all.get(2).start == 780 && all.get(2).end == 840,
                "afternoon gap invalid");

        List<WidgetBreakSequence.Segment> noLunch = WidgetBreakSequence.between(
                11 * 60, 14 * 60, 12 * 60, 13 * 60, true, false, -1);
        require(noLunch.size() == 2 && noLunch.stream().allMatch(x -> x.type == WidgetBreakSequence.GAP),
                "lunch switch must hide only lunch");

        List<WidgetBreakSequence.Segment> noGaps = WidgetBreakSequence.between(
                11 * 60, 14 * 60, 12 * 60, 13 * 60, false, true, -1);
        require(noGaps.size() == 1 && noGaps.get(0).type == WidgetBreakSequence.LUNCH,
                "gap switch must hide only gaps");

        List<WidgetBreakSequence.Segment> clipped = WidgetBreakSequence.between(
                11 * 60, 14 * 60, 12 * 60, 13 * 60, true, true, 11 * 60 + 35);
        require(clipped.get(0).start == 11 * 60 + 35, "current gap must start now");

        List<WidgetBreakSequence.Segment> disabledLunchDay = WidgetBreakSequence.between(
                11 * 60, 14 * 60, 12 * 60, 12 * 60, true, true, -1);
        require(disabledLunchDay.size() == 1 && disabledLunchDay.get(0).type == WidgetBreakSequence.GAP,
                "disabled lunch day must remain a regular gap");

        System.out.println("widget_break_sequence_663=passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
