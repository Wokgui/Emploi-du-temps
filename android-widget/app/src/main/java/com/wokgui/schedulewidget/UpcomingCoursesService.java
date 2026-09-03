package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UpcomingCoursesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static final class Item {
        final String label;
        final String time;
        final String room;
        final boolean lunch;

        Item(String label, String time, String room, boolean lunch) {
            this.label = label;
            this.time = time;
            this.room = room;
            this.lunch = lunch;
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

            Calendar targetDate;
            int anchorStart;
            boolean mainShowsLunch = false;

            if (current != null) {
                targetDate = (Calendar) now.clone();
                anchorStart = ScheduleData.toMinutes(current.start);
            } else if (inLunch) {
                targetDate = (Calendar) now.clone();
                anchorStart = lunchStart;
                mainShowsLunch = true;
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
                anchorStart = ScheduleData.toMinutes(topNext.start);
            }

            List<ScheduleData.Course> sameDay = ScheduleStore.getCourses(
                    context, targetDate.get(Calendar.DAY_OF_WEEK));

            boolean hasAfternoonCourse = false;
            for (ScheduleData.Course c : sameDay) {
                if (ScheduleData.toMinutes(c.start) >= lunchEnd) {
                    hasAfternoonCourse = true;
                    break;
                }
            }

            boolean lunchAdded = mainShowsLunch;
            for (ScheduleData.Course c : sameDay) {
                int start = ScheduleData.toMinutes(c.start);
                if (start <= anchorStart) continue;

                if (!lunchAdded && lunchValid && hasAfternoonCourse
                        && anchorStart < lunchStart && start >= lunchEnd) {
                    items.add(new Item(
                            "Pause de midi",
                            ScheduleStore.getSlotEnd(context, 4) + "–" + ScheduleStore.getSlotStart(context, 5),
                            "",
                            true));
                    lunchAdded = true;
                }

                items.add(new Item(c.label, c.start, c.room, false));
            }
        }

        @Override public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            v.setTextViewText(R.id.rowTitle, item.label);
            if (item.lunch) {
                v.setTextViewText(R.id.rowMeta, item.time + " · reprise à " + ScheduleStore.getSlotStart(context, 5));
                v.setTextColor(R.id.rowTitle, 0xFF9A5C09);
                v.setTextColor(R.id.rowMeta, 0xFF8B6A3A);
            } else {
                v.setTextViewText(R.id.rowMeta, item.time + " · salle " + (item.room.isEmpty() ? "—" : item.room));
                v.setTextColor(R.id.rowTitle, 0xFF101936);
                v.setTextColor(R.id.rowMeta, 0xFF647087);
            }

            Intent fill = new Intent();
            fill.putExtra("open_mode", item.lunch ? "today" : "edit");
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
