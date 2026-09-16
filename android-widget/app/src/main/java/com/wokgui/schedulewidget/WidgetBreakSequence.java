package com.wokgui.schedulewidget;

import java.util.ArrayList;
import java.util.List;

/** Pure break planner shared by the standard and condensed widgets. */
final class WidgetBreakSequence {
    static final int GAP = 1;
    static final int LUNCH = 2;

    static final class Segment {
        final int type;
        final int start;
        final int end;

        Segment(int type, int start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }

    private WidgetBreakSequence() {}

    static List<Segment> between(int from, int to, int lunchStart, int lunchEnd,
                                 boolean showGaps, boolean showLunch, int cutoffMinute) {
        List<Segment> result = new ArrayList<>();
        if (to <= from) return result;

        boolean lunchValid = lunchEnd > lunchStart && from <= lunchStart && to >= lunchEnd;
        if (!lunchValid) {
            addGap(result, from, to, showGaps, cutoffMinute);
            return result;
        }

        addGap(result, from, lunchStart, showGaps, cutoffMinute);
        if (showLunch && (cutoffMinute < 0 || lunchEnd > cutoffMinute)) {
            result.add(new Segment(LUNCH, lunchStart, lunchEnd));
        }
        addGap(result, lunchEnd, to, showGaps, cutoffMinute);
        return result;
    }

    private static void addGap(List<Segment> result, int start, int end,
                               boolean showGaps, int cutoffMinute) {
        if (!showGaps || end <= start || (cutoffMinute >= 0 && end <= cutoffMinute)) return;
        result.add(new Segment(GAP, Math.max(start, cutoffMinute), end));
    }
}
