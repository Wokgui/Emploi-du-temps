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

        Item(String label, String time, String room) {
            this.label = label;
            this.time = time;
            this.room = room;
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

            // Le cours affiché en grand par le widget est le prochain cours.
            // On cherche ce cours, puis on ne met dans la liste que les cours
            // qui le suivent LE MÊME JOUR. Aucun cours d'un autre jour n'est ajouté.
            Calendar targetDate = null;
            ScheduleData.Course topNext = null;

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

            List<ScheduleData.Course> sameDay = ScheduleStore.getCourses(
                    context,
                    targetDate.get(Calendar.DAY_OF_WEEK)
            );
            int topStart = ScheduleData.toMinutes(topNext.start);

            for (ScheduleData.Course c : sameDay) {
                if (ScheduleData.toMinutes(c.start) > topStart) {
                    items.add(new Item(c.label, c.start, c.room));
                }
            }
        }

        @Override public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            v.setTextViewText(R.id.rowTitle, item.label);
            v.setTextViewText(R.id.rowMeta, item.time + " · salle " + (item.room.isEmpty() ? "—" : item.room));

            Intent fill = new Intent();
            fill.putExtra("open_mode", "edit");
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
