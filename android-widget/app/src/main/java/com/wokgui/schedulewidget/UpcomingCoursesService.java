package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpcomingCoursesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static final class Item {
        static final int COURSE = 0;
        static final int LUNCH = 1;
        static final int GAP = 2;

        final String label;
        final String time;
        final String room;
        final int type;

        Item(String label, String time, String room, int type) {
            this.label = label;
            this.time = time;
            this.room = room;
            this.type = type;
        }
    }

    private static final class GapInfo {
        final int start;
        final int end;
        GapInfo(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private final List<Item> items = new ArrayList<>();

        Factory(Context context) { this.context = context; }
        @Override public void onCreate() { reload(); }
        @Override public void onDataSetChanged() { reload(); }
        @Override public void onDestroy() { items.clear(); }
        @Override public int getCount() { return items.size(); }

        private void reload() {
            items.clear();
            ScheduleStore.ensureInitialized(context);

            Calendar now = Calendar.getInstance();
            int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            int lunchStart = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4));
            int lunchEnd = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5));
            boolean lunchValid = lunchEnd > lunchStart;

            List<ScheduleData.Course> todayCourses = ScheduleStore.getCourses(
                    context, now.get(Calendar.DAY_OF_WEEK));
            ScheduleData.Course current = null;
            for (ScheduleData.Course c : todayCourses) {
                int s = ScheduleData.toMinutes(c.start);
                int e = ScheduleData.toMinutes(c.end);
                if (nowMin >= s && nowMin < e) {
                    current = c;
                    break;
                }
            }

            boolean inLunch = current == null && !todayCourses.isEmpty() && lunchValid
                    && nowMin >= lunchStart && nowMin < lunchEnd;
            GapInfo currentGap = current == null && !inLunch
                    ? findCurrentGap(todayCourses, nowMin, lunchStart, lunchEnd)
                    : null;

            Calendar targetDate;
            int threshold;
            int previousEnd;

            if (current != null) {
                targetDate = (Calendar) now.clone();
                threshold = ScheduleData.toMinutes(current.start) + 1;
                previousEnd = ScheduleData.toMinutes(current.end);
            } else if (inLunch) {
                targetDate = (Calendar) now.clone();
                threshold = lunchEnd;
                previousEnd = lunchEnd;
            } else if (currentGap != null) {
                targetDate = (Calendar) now.clone();
                threshold = currentGap.end;
                previousEnd = currentGap.end;
            } else {
                ScheduleData.Course topNext = null;
                targetDate = null;
                Calendar cursor = (Calendar) now.clone();
                for (int add = 0; add < 8 && topNext == null; add++) {
                    int day = cursor.get(Calendar.DAY_OF_WEEK);
                    List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, day);
                    for (ScheduleData.Course c : courses) {
                        if (add == 0 && ScheduleData.toMinutes(c.start) <= nowMin) continue;
                        topNext = c;
                        targetDate = (Calendar) cursor.clone();
                        break;
                    }
                    cursor.add(Calendar.DAY_OF_YEAR, 1);
                }
                if (topNext == null || targetDate == null) return;
                threshold = ScheduleData.toMinutes(topNext.start) + 1;
                previousEnd = ScheduleData.toMinutes(topNext.end);
            }

            List<ScheduleData.Course> sameDay = ScheduleStore.getCourses(
                    context, targetDate.get(Calendar.DAY_OF_WEEK));

            for (ScheduleData.Course c : sameDay) {
                int start = ScheduleData.toMinutes(c.start);
                if (start < threshold) continue;

                appendBreaks(previousEnd, start, lunchStart, lunchEnd);
                items.add(new Item(c.label, c.start, c.room, Item.COURSE));
                previousEnd = ScheduleData.toMinutes(c.end);
            }
        }

        private GapInfo findCurrentGap(List<ScheduleData.Course> courses, int minute,
                                       int lunchStart, int lunchEnd) {
            if (courses == null || courses.size() < 2) return null;

            int previousEnd = -1;
            int nextStart = Integer.MAX_VALUE;
            for (ScheduleData.Course c : courses) {
                int start = ScheduleData.toMinutes(c.start);
                int end = ScheduleData.toMinutes(c.end);
                if (end <= minute && end > previousEnd) previousEnd = end;
                if (start > minute && start < nextStart) nextStart = start;
            }
            if (previousEnd < 0 || nextStart == Integer.MAX_VALUE || nextStart <= previousEnd) return null;

            boolean lunchValid = lunchEnd > lunchStart;
            if (lunchValid && minute >= lunchStart && minute < lunchEnd) return null;

            int start = previousEnd;
            int end = nextStart;
            if (lunchValid) {
                if (minute < lunchStart && end > lunchStart) end = lunchStart;
                else if (minute >= lunchEnd && start < lunchEnd) start = lunchEnd;
            }
            if (end <= start || minute < start || minute >= end) return null;
            return new GapInfo(start, end);
        }

        private void appendBreaks(int from, int to, int lunchStart, int lunchEnd) {
            if (to <= from) return;
            boolean lunchValid = lunchEnd > lunchStart;

            if (!lunchValid || to <= lunchStart || from >= lunchEnd) {
                addGap(from, to);
                return;
            }

            if (from < lunchStart) addGap(from, Math.min(to, lunchStart));

            if (from <= lunchStart && to >= lunchEnd) {
                items.add(new Item(
                        "Pause de midi",
                        minuteLabel(lunchStart) + "–" + minuteLabel(lunchEnd),
                        "",
                        Item.LUNCH));
            }

            if (to > lunchEnd) addGap(Math.max(from, lunchEnd), to);
        }

        private void addGap(int start, int end) {
            int duration = end - start;
            if (duration <= 0) return;
            items.add(new Item(
                    "Trou · " + durationLabel(duration),
                    minuteLabel(start) + "–" + minuteLabel(end),
                    "",
                    Item.GAP));
        }

        private String minuteLabel(int minute) {
            return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60);
        }

        private String durationLabel(int minutes) {
            int h = minutes / 60;
            int m = minutes % 60;
            if (h > 0 && m > 0) return h + " h " + m;
            if (h > 0) return h + " h";
            return m + " min";
        }

        @Override public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            v.setTextViewText(R.id.rowTitle, item.label);

            if (item.type == Item.LUNCH) {
                v.setTextViewText(R.id.rowMeta,
                        item.time + " · reprise à " + ScheduleStore.getSlotStart(context, 5));
                v.setTextColor(R.id.rowTitle, 0xFF9A5C09);
                v.setTextColor(R.id.rowMeta, 0xFF8B6A3A);
            } else if (item.type == Item.GAP) {
                v.setTextViewText(R.id.rowMeta, item.time + " · sans cours");
                v.setTextColor(R.id.rowTitle, 0xFF7357B8);
                v.setTextColor(R.id.rowMeta, 0xFF6F6287);
            } else {
                v.setTextViewText(R.id.rowMeta,
                        item.time + " · salle " + (item.room.isEmpty() ? "—" : item.room));
                v.setTextColor(R.id.rowTitle, 0xFF101936);
                v.setTextColor(R.id.rowMeta, 0xFF647087);
            }

            Intent fill = new Intent();
            fill.putExtra("open_mode", item.type == Item.COURSE ? "edit" : "today");
            v.setOnClickFillInIntent(R.id.rowTitle, fill);
            v.setOnClickFillInIntent(R.id.rowMeta, fill);
            return v;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}
