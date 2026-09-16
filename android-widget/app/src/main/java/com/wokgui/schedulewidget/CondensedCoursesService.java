package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** RemoteViews source for format 3: a compact chronological day list. */
public final class CondensedCoursesService extends RemoteViewsService {
    static RemoteViewsFactory createFactory(Context context, int widgetId) {
        return new Factory(context.getApplicationContext(), widgetId);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent == null ? AppWidgetManager.INVALID_APPWIDGET_ID
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return createFactory(getApplicationContext(), widgetId);
    }

    private static final class Item {
        final String label;
        final String sourceLabel;
        final String time;
        final String room;
        final String relative;
        final String colorId;
        final int order;
        final boolean uncertain;

        Item(String label, String sourceLabel, String time, String room, int order,
             String relative, boolean uncertain, String colorId) {
            this.label = label;
            this.sourceLabel = sourceLabel == null ? label : sourceLabel;
            this.time = time;
            this.room = room == null ? "" : room;
            this.order = order;
            this.relative = relative == null ? "" : relative;
            this.uncertain = uncertain;
            this.colorId = colorId == null ? "" : colorId;
        }
    }

    private static final class Target {
        final Calendar date;
        final int firstCourse;

        Target(Calendar date, int firstCourse) {
            this.date = date;
            this.firstCourse = firstCourse;
        }
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private final int widgetId;
        private final List<Item> items = new ArrayList<>();
        private int widgetHeightDp = 49;

        Factory(Context context, int widgetId) {
            this.context = context;
            this.widgetId = widgetId;
        }

        @Override public void onCreate() { reload(); }
        @Override public void onDataSetChanged() { reload(); }
        @Override public void onDestroy() { items.clear(); }
        @Override public int getCount() { return items.size(); }

        private void reload() {
            items.clear();
            ScheduleStore.ensureInitialized(context);
            widgetHeightDp = resolveWidgetHeightDp();

            Calendar now = Calendar.getInstance();
            int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            Target target = resolveTarget(now, nowMin);
            if (target == null) return;

            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, target.date);
            if (courses == null || courses.isEmpty() || target.firstCourse >= courses.size()) return;

            boolean futureDay = !sameDay(now, target.date);
            boolean firstVisibleCourse = true;

            for (int i = target.firstCourse; i < courses.size(); i++) {
                ScheduleData.Course course = courses.get(i);
                int order = course.slot > 0 ? course.slot : i + 1;
                String displayLabel = AdvancedSettingsStore.widgetCourseLabel(context, target.date, course);
                items.add(new Item(
                        displayLabel,
                        course.label,
                        course.start + " - " + course.end,
                        course.room,
                        order,
                        relativeLabel(now, target.date, course, futureDay && firstVisibleCourse),
                        course.uncertain,
                        course.color
                ));
                firstVisibleCourse = false;
            }
        }

        private int resolveWidgetHeightDp() {
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return 49;
            Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            if (options == null) return 49;
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            return WidgetHeightSizing.resolveHeightDp(
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
                    landscape,
                    49
            );
        }

        private Target resolveTarget(Calendar now, int nowMin) {
            List<ScheduleData.Course> today = ScheduleStore.getCourses(context, now);
            if (today != null) {
                for (int i = 0; i < today.size(); i++) {
                    if (ScheduleData.toMinutes(today.get(i).end) > nowMin) {
                        return new Target((Calendar) now.clone(), i);
                    }
                }
            }

            Calendar cursor = (Calendar) now.clone();
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            for (int add = 0; add < 21; add++) {
                List<ScheduleData.Course> list = ScheduleStore.getCourses(context, cursor);
                if (list != null && !list.isEmpty()) return new Target((Calendar) cursor.clone(), 0);
                cursor.add(Calendar.DAY_OF_YEAR, 1);
            }
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);

            float scale = UiSettingsStore.widgetFontScale(context);
            views.setTextViewTextSize(R.id.rowCondensedTime, TypedValue.COMPLEX_UNIT_SP, 8f * scale);
            views.setTextViewTextSize(R.id.rowCondensedTitle, TypedValue.COMPLEX_UNIT_SP, 10f * scale);

            views.setViewVisibility(R.id.rowContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedContent, View.VISIBLE);
            views.setViewVisibility(R.id.rowMiniContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedLineTop, position == 0 ? View.GONE : View.VISIBLE);
            views.setViewVisibility(R.id.rowCondensedLineBottom, position == items.size() - 1 ? View.GONE : View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                int fittedHeight = CondensedRowSizing.rowHeightDp(widgetHeightDp, items.size(), position);
                views.setViewLayoutHeight(R.id.rowRoot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedContent, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                int halfLine = Math.max(14, fittedHeight / 2);
                views.setViewLayoutHeight(R.id.rowCondensedLineTop, halfLine, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedLineBottom, halfLine, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedAccent, Math.max(18, fittedHeight - 8), TypedValue.COMPLEX_UNIT_DIP);
            }

            String title = item.label;
            if (item.uncertain) title = "⚠ " + title;
            String meta = condensedMeta(item);
            if (!meta.isEmpty()) title += " · " + meta;
            views.setTextViewText(R.id.rowCondensedTitle, title);
            views.setTextViewText(R.id.rowCondensedTime, AdvancedSettingsStore.showTimes(context) ? startTime(item.time) : "");
            views.setTextViewText(R.id.rowCondensedMeta, "");
            views.setViewVisibility(R.id.rowCondensedMeta, View.GONE);

            int accent = WidgetPaletteStore.courseColor(context, item.order, item.sourceLabel, item.colorId);
            views.setInt(R.id.rowCondensedAccent, "setBackgroundColor", accent);

            Intent fill = new Intent();
            fill.putExtra("open_mode", "week");
            views.setOnClickFillInIntent(R.id.rowRoot, fill);
            views.setOnClickFillInIntent(R.id.rowCondensedContent, fill);
            views.setOnClickFillInIntent(R.id.rowCondensedTime, fill);
            views.setOnClickFillInIntent(R.id.rowCondensedTitle, fill);
            views.setOnClickFillInIntent(R.id.rowCondensedMeta, fill);
            return views;
        }

        private String condensedMeta(Item item) {
            List<String> parts = new ArrayList<>();
            if (AdvancedSettingsStore.showRoom(context)) {
                parts.add(UiSettingsStore.t(context, "room") + " " + (item.room.isEmpty() ? "—" : item.room));
            }
            if (AdvancedSettingsStore.showRemaining(context) && !item.relative.isEmpty()) parts.add(item.relative);
            return String.join(" · ", parts);
        }

        private String startTime(String range) {
            int cut = range == null ? -1 : range.indexOf(" - ");
            return cut > 0 ? range.substring(0, cut) : (range == null ? "" : range);
        }

        private String relativeLabel(Calendar now, Calendar targetDate, ScheduleData.Course course, boolean includeDate) {
            Calendar start = atMinute(targetDate, ScheduleData.toMinutes(course.start));
            Calendar end = atMinute(targetDate, ScheduleData.toMinutes(course.end));
            long nowMs = now.getTimeInMillis();
            if (nowMs >= start.getTimeInMillis() && nowMs < end.getTimeInMillis()) {
                long rem = Math.max(1L, (end.getTimeInMillis() - nowMs + 59999L) / 60000L);
                return rem + " min";
            }
            long diff = Math.max(0L, (start.getTimeInMillis() - nowMs) / 60000L);
            if (diff <= 0) return "";
            String base = diff < 60
                    ? UiSettingsStore.t(context, "in") + " " + diff + " min"
                    : UiSettingsStore.t(context, "in") + " " + Math.max(1L, Math.round(diff / 60.0)) + " h";
            return includeDate ? base + dateSuffix(targetDate) : base;
        }

        private String dateSuffix(Calendar date) {
            String lang = UiSettingsStore.language(context);
            Locale locale = "de".equals(lang) ? Locale.GERMANY : ("en".equals(lang) ? Locale.UK : Locale.FRANCE);
            if ("de".equals(lang)) return ", am " + new SimpleDateFormat("EEEE, d. MMM.", locale).format(date.getTime());
            if ("en".equals(lang)) return ", " + new SimpleDateFormat("EEE d MMM", locale).format(date.getTime());
            return ", le " + new SimpleDateFormat("EEEE d MMM.", locale).format(date.getTime());
        }

        private Calendar atMinute(Calendar date, int minute) {
            Calendar out = (Calendar) date.clone();
            out.set(Calendar.HOUR_OF_DAY, minute / 60);
            out.set(Calendar.MINUTE, minute % 60);
            out.set(Calendar.SECOND, 0);
            out.set(Calendar.MILLISECOND, 0);
            return out;
        }

        private boolean sameDay(Calendar a, Calendar b) {
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                    && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}
