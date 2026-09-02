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
        final int day;
        final String label;
        final String time;
        final String room;
        Item(int day, String label, String time, String room) {
            this.day = day; this.label = label; this.time = time; this.room = room;
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
            int today = now.get(Calendar.DAY_OF_WEEK);
            boolean hasCurrent = false;
            for (ScheduleData.Course c : ScheduleStore.getCourses(context, today)) {
                int s = ScheduleData.toMinutes(c.start), e = ScheduleData.toMinutes(c.end);
                if (nowMin >= s && nowMin < e) { hasCurrent = true; break; }
            }
            boolean skippedTopNext = hasCurrent;
            Calendar cursor = (Calendar) now.clone();
            for (int add = 0; add < 8 && items.size() < 30; add++) {
                int day = cursor.get(Calendar.DAY_OF_WEEK);
                List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, day);
                for (ScheduleData.Course c : courses) {
                    if (add == 0 && ScheduleData.toMinutes(c.start) <= nowMin) continue;
                    if (!skippedTopNext) { skippedTopNext = true; continue; }
                    items.add(new Item(day, c.label, c.start, c.room));
                }
                cursor.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        @Override public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            v.setTextViewText(R.id.rowTitle, item.label);
            String when = item.day == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
                    ? item.time
                    : shortDay(item.day) + " " + item.time;
            v.setTextViewText(R.id.rowMeta, when + " · salle " + (item.room.isEmpty() ? "—" : item.room));
            Intent fill = new Intent();
            fill.putExtra("open_mode", "edit");
            v.setOnClickFillInIntent(R.id.rowTitle, fill);
            v.setOnClickFillInIntent(R.id.rowMeta, fill);
            return v;
        }

        private String shortDay(int day) {
            switch (day) {
                case Calendar.MONDAY: return "Lun";
                case Calendar.TUESDAY: return "Mar";
                case Calendar.WEDNESDAY: return "Mer";
                case Calendar.THURSDAY: return "Jeu";
                case Calendar.FRIDAY: return "Ven";
                default: return "";
            }
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}
