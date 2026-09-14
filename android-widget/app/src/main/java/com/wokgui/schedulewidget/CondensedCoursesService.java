package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
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
        static final int COURSE = 0;
        static final int LUNCH = 1;
        static final int GAP = 2;

        final String label;
        final String sourceLabel;
        final String time;
        final String room;
        final String relative;
        final String colorId;
        final int type;
        final int order;
        final boolean uncertain;

        Item(String label, String sourceLabel, String time, String room, int type, int order,
             String relative, boolean uncertain, String colorId) {
            this.label = label;
            this.sourceLabel = sourceLabel == null ? label : sourceLabel;
            this.time = time;
            this.room = room == null ? "" : room;
            this.type = type;
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
        @SuppressWarnings("unused")
        private final int widgetId;
        private final List<Item> items = new ArrayList<>();

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

            Calendar now = Calendar.getInstance();
            int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            Target target = resolveTarget(now, nowMin);
            if (target == null) return;

            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, target.date);
            if (courses == null || courses.isEmpty() || target.firstCourse >= courses.size()) return;

            int lunchStart = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4));
            int lunchEnd = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5));
            boolean futureDay = !sameDay(now, target.date);
            int previousEnd = -1;
            boolean firstVisibleCourse = true;

            if (!futureDay && target.firstCourse > 0) {
                ScheduleData.Course previous = courses.get(target.firstCourse - 1);
                ScheduleData.Course next = courses.get(target.firstCourse);
                int from = ScheduleData.toMinutes(previous.end);
                int to = ScheduleData.toMinutes(next.start);
                if (nowMin >= from && nowMin < to) appendBreaks(from, to, lunchStart, lunchEnd, nowMin);
            }

            for (int i = target.firstCourse; i < courses.size(); i++) {
                ScheduleData.Course course = courses.get(i);
                int start = ScheduleData.toMinutes(course.start);
                if (previousEnd >= 0) appendBreaks(previousEnd, start, lunchStart, lunchEnd, -1);

                int order = course.slot > 0 ? course.slot : i + 1;
                String displayLabel = AdvancedSettingsStore.widgetCourseLabel(context, target.date, course);
                items.add(new Item(
                        displayLabel,
                        course.label,
                        course.start + " - " + course.end,
                        course.room,
                        Item.COURSE,
                        order,
                        relativeLabel(now, target.date, course, futureDay && firstVisibleCourse),
                        course.uncertain,
                        course.color
                ));
                previousEnd = ScheduleData.toMinutes(course.end);
                firstVisibleCourse = false;
            }
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

        private void appendBreaks(int from, int to, int lunchStart, int lunchEnd, int cutoffMinute) {
            if (to <= from) return;
            boolean lunchValid = lunchEnd > lunchStart;
            if (!lunchValid || to <= lunchStart || from >= lunchEnd) {
                addGap(from, to, cutoffMinute);
                return;
            }
            if (from < lunchStart) addGap(from, Math.min(to, lunchStart), cutoffMinute);
            if (from <= lunchStart && to >= lunchEnd
                    && AdvancedSettingsStore.showLunch(context)
                    && (cutoffMinute < 0 || lunchEnd > cutoffMinute)) {
                String source = lunchLabel();
                items.add(new Item(
                        AdvancedSettingsStore.widgetLunchLabel(context, source),
                        source,
                        minuteLabel(lunchStart) + " - " + minuteLabel(lunchEnd),
                        "",
                        Item.LUNCH,
                        0,
                        lunchRelative(lunchEnd),
                        false,
                        ""
                ));
            }
            if (to > lunchEnd) addGap(Math.max(from, lunchEnd), to, cutoffMinute);
        }

        private void addGap(int start, int end, int cutoffMinute) {
            if (!AdvancedSettingsStore.showBreaks(context) || end <= start) return;
            if (cutoffMinute >= 0 && end <= cutoffMinute) return;
            String source = gapLabel();
            items.add(new Item(
                    AdvancedSettingsStore.widgetGapLabel(context, source),
                    source,
                    minuteLabel(start) + " - " + minuteLabel(end),
                    "",
                    Item.GAP,
                    0,
                    gapRelative(start, end),
                    false,
                    ""
            ));
        }

        private String lunchLabel() {
            String value = ScheduleStore.getLunchLabel(context);
            return "Pause de midi".equalsIgnoreCase(value) ? "Midi" : value;
        }

        private String gapLabel() {
            String value = ScheduleStore.getGapLabel(context);
            return "Trou".equalsIgnoreCase(value) ? UiSettingsStore.t(context, "gap") : value;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);

            views.setViewVisibility(R.id.rowContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedContent, View.VISIBLE);
            views.setViewVisibility(R.id.rowMiniContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedLineTop, position == 0 ? View.GONE : View.VISIBLE);
            views.setViewVisibility(R.id.rowCondensedLineBottom, position == items.size() - 1 ? View.GONE : View.VISIBLE);

            String title = item.label;
            if (item.type == Item.COURSE && item.uncertain) title = "⚠ " + title;
            views.setTextViewText(R.id.rowCondensedTitle, title);
            views.setTextViewText(R.id.rowCondensedTime, AdvancedSettingsStore.showTimes(context) ? startTime(item.time) : "");

            String meta = condensedMeta(item);
            views.setTextViewText(R.id.rowCondensedMeta, meta);
            views.setViewVisibility(R.id.rowCondensedMeta, meta.isEmpty() ? View.GONE : View.VISIBLE);

            int accent;
            if (item.type == Item.LUNCH) {
                accent = darken(WidgetPaletteStore.lunchBackground(context));
            } else if (item.type == Item.GAP) {
                accent = darken(WidgetPaletteStore.gapBackground(context));
            } else {
                accent = WidgetPaletteStore.courseColor(context, item.order, item.sourceLabel, item.colorId);
            }
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
            if (item.type == Item.COURSE && AdvancedSettingsStore.showRoom(context)) {
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

        private String lunchRelative(int endMinute) {
            String lang = UiSettingsStore.language(context);
            String time = "fr".equals(lang) && endMinute % 60 == 0
                    ? (endMinute / 60) + " h"
                    : minuteLabel(endMinute);
            return UiSettingsStore.t(context, "backAt") + " " + time;
        }

        private String gapRelative(int start, int end) {
            int duration = Math.max(0, end - start);
            String lang = UiSettingsStore.language(context);
            if (duration % 60 == 0 && duration >= 60) {
                int hours = duration / 60;
                if ("de".equals(lang)) return "Frei " + hours + " Std.";
                if ("en".equals(lang)) return "Free " + hours + " h";
                return "Libre " + hours + " h";
            }
            if ("de".equals(lang)) return "Frei " + duration + " Min.";
            if ("en".equals(lang)) return "Free " + duration + " min";
            return "Libre " + duration + " min";
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

        private String minuteLabel(int minute) {
            return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60);
        }

        private int darken(int color) {
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            r = Math.max(45, (int) (r * .62f));
            g = Math.max(45, (int) (g * .62f));
            b = Math.max(45, (int) (b * .62f));
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}
