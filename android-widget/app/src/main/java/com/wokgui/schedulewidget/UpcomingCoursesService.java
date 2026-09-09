package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UpcomingCoursesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent == null ? AppWidgetManager.INVALID_APPWIDGET_ID
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new Factory(getApplicationContext(), widgetId);
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
            this.room = room;
            this.type = type;
            this.order = order;
            this.relative = relative;
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
        private boolean compactHeight;
        private Calendar currentTargetDate;

        Factory(Context context, int widgetId) {
            this.context = context;
            this.widgetId = widgetId;
        }

        @Override public void onCreate() { reload(); }
        @Override public void onDataSetChanged() { reload(); }
        @Override public void onDestroy() { items.clear(); }
        @Override public int getCount() { return items.size(); }

        private boolean isCompactHeight() {
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return false;
            Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            int h = options == null ? 120 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 120);
            return h > 0 && h <= 145;
        }

        private void reload() {
            items.clear();
            ScheduleStore.ensureInitialized(context);
            compactHeight = isCompactHeight();

            Calendar now = Calendar.getInstance();
            int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            Target target = resolveTarget(now, nowMin);
            if (target == null) return;
            currentTargetDate = (Calendar) target.date.clone();

            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, target.date);
            if (courses == null || courses.isEmpty() || target.firstCourse >= courses.size()) return;

            int lunchStart = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4));
            int lunchEnd = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5));
            boolean futureDay = !sameDay(now, target.date);
            int previousEnd = -1;
            boolean firstVisibleCourse = true;

            for (int i = target.firstCourse; i < courses.size(); i++) {
                ScheduleData.Course c = courses.get(i);
                int start = ScheduleData.toMinutes(c.start);
                if (previousEnd >= 0) appendBreaks(previousEnd, start, lunchStart, lunchEnd);

                int order = c.slot > 0 ? c.slot : i + 1;
                String relative = relativeLabel(now, target.date, c, futureDay && firstVisibleCourse);
                String displayLabel = AdvancedSettingsStore.widgetCourseLabel(context, target.date, c);
                items.add(new Item(
                        displayLabel,
                        c.label,
                        c.start + " - " + c.end,
                        c.room,
                        Item.COURSE,
                        order,
                        relative,
                        c.uncertain,
                        c.color
                ));
                firstVisibleCourse = false;
                previousEnd = ScheduleData.toMinutes(c.end);
            }
        }

        private Target resolveTarget(Calendar now, int nowMin) {
            List<ScheduleData.Course> today = ScheduleStore.getCourses(context, now);
            if (today != null) {
                for (int i = 0; i < today.size(); i++) {
                    ScheduleData.Course c = today.get(i);
                    if (ScheduleData.toMinutes(c.end) > nowMin) return new Target((Calendar) now.clone(), i);
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

        private void appendBreaks(int from, int to, int lunchStart, int lunchEnd) {
            if (to <= from) return;
            boolean lunchValid = lunchEnd > lunchStart;
            if (!lunchValid || to <= lunchStart || from >= lunchEnd) {
                addGap(from, to);
                return;
            }
            if (from < lunchStart) addGap(from, Math.min(to, lunchStart));
            if (from <= lunchStart && to >= lunchEnd && AdvancedSettingsStore.showLunch(context)) {
                String appLabel = localizedAppBreakLabel(true);
                String label = AdvancedSettingsStore.widgetLunchLabel(context, appLabel);
                items.add(new Item(
                        label,
                        appLabel,
                        minuteLabel(lunchStart) + " - " + minuteLabel(lunchEnd),
                        "",
                        Item.LUNCH,
                        0,
                        lunchRelative(lunchEnd),
                        false,
                        ""
                ));
            }
            if (to > lunchEnd) addGap(Math.max(from, lunchEnd), to);
        }

        private void addGap(int start, int end) {
            if (!AdvancedSettingsStore.showBreaks(context) || end <= start) return;
            String appLabel = localizedAppBreakLabel(false);
            String label = AdvancedSettingsStore.widgetGapLabel(context, appLabel);
            items.add(new Item(
                    label,
                    appLabel,
                    minuteLabel(start) + " - " + minuteLabel(end),
                    "",
                    Item.GAP,
                    0,
                    gapRelative(start, end),
                    false,
                    ""
            ));
        }

        private String localizedAppBreakLabel(boolean lunch) {
            String custom = lunch ? ScheduleStore.getLunchLabel(context) : ScheduleStore.getGapLabel(context);
            if (lunch && "Pause de midi".equalsIgnoreCase(custom)) return "Midi";
            if (!lunch && "Trou".equalsIgnoreCase(custom)) return UiSettingsStore.t(context, "gap");
            return custom;
        }

        private String lunchRelative(int endMinute) {
            String lang = UiSettingsStore.language(context);
            String time;
            if ("fr".equals(lang) && endMinute % 60 == 0) time = (endMinute / 60) + " h";
            else time = minuteLabel(endMinute);
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

            String base;
            if (diff < 60) base = UiSettingsStore.t(context, "in") + " " + diff + " min";
            else {
                long hours = Math.max(1L, Math.round(diff / 60.0));
                base = UiSettingsStore.t(context, "in") + " " + hours + " h";
            }

            if (includeDate) base += dateSuffix(targetDate);
            return base;
        }

        private Calendar atMinute(Calendar date, int minute) {
            Calendar out = (Calendar) date.clone();
            out.set(Calendar.HOUR_OF_DAY, minute / 60);
            out.set(Calendar.MINUTE, minute % 60);
            out.set(Calendar.SECOND, 0);
            out.set(Calendar.MILLISECOND, 0);
            return out;
        }

        private String dateSuffix(Calendar date) {
            String lang = UiSettingsStore.language(context);
            Locale locale = "de".equals(lang) ? Locale.GERMANY : ("en".equals(lang) ? Locale.UK : Locale.FRANCE);
            if ("fr".equals(lang) || (!"de".equals(lang) && !"en".equals(lang))) {
                String d = new SimpleDateFormat("EEEE d MMM.", locale).format(date.getTime());
                return ",\nle " + d;
            }
            if ("de".equals(lang)) return ",\nam " + new SimpleDateFormat("EEEE, d. MMM.", locale).format(date.getTime());
            return ",\n" + new SimpleDateFormat("EEE d MMM", locale).format(date.getTime());
        }

        private boolean sameDay(Calendar a, Calendar b) {
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                    && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
        }

        private String minuteLabel(int minute) {
            return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60);
        }

        private String courseMeta(Item item) {
            boolean times = AdvancedSettingsStore.showTimes(context);
            boolean room = AdvancedSettingsStore.showRoom(context);
            if (times && room) return item.time + " · " + UiSettingsStore.t(context, "room") + " " + (item.room.isEmpty() ? "—" : item.room);
            if (times) return item.time;
            if (room) return UiSettingsStore.t(context, "room") + " " + (item.room.isEmpty() ? "—" : item.room);
            return "";
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);

            float scale = UiSettingsStore.widgetFontScale(context);
            v.setTextViewTextSize(R.id.rowTitle, TypedValue.COMPLEX_UNIT_SP, 13f * scale);
            v.setTextViewTextSize(R.id.rowMeta, TypedValue.COMPLEX_UNIT_SP, 10f * scale);
            v.setTextViewTextSize(R.id.rowRelative, TypedValue.COMPLEX_UNIT_SP, 9f * scale);

            v.setViewVisibility(R.id.rowIndex, View.GONE);
            v.setViewVisibility(R.id.rowDot, View.GONE);
            v.setViewVisibility(R.id.rowLineTop, View.GONE);
            v.setViewVisibility(R.id.rowLineBottom, View.GONE);

            String title = item.label;
            if (item.type == Item.LUNCH) title = "🍴 " + title;
            if (item.type == Item.COURSE && item.uncertain) title = "⚠ " + title;
            v.setTextViewText(R.id.rowTitle, title);
            v.setTextViewText(R.id.rowRelative, item.relative);
            v.setViewVisibility(R.id.rowRelative, item.relative.isEmpty() ? View.GONE : View.VISIBLE);
            v.setInt(R.id.rowRelative, "setGravity", Gravity.CENTER);

            if (item.type == Item.LUNCH) {
                int bg = WidgetPaletteStore.lunchBackground(context);
                applyBreakRow(v, item, bg, WidgetPaletteStore.lunchText(context), darken(bg));
            } else if (item.type == Item.GAP) {
                int bg = WidgetPaletteStore.gapBackground(context);
                int ink = WidgetPaletteStore.gapText(context);
                applyBreakRow(v, item, bg, ink, ink);
            } else {
                int bg = WidgetPaletteStore.courseColor(context, item.order, item.sourceLabel, item.colorId);
                boolean dark = WidgetPaletteStore.useDarkText(context, item.order, item.sourceLabel, item.colorId);
                int ink = dark ? 0xFF17213A : 0xFFFFFFFF;
                int muted = dark ? 0xFF35435A : 0xFFF7FBFF;
                v.setInt(R.id.rowContent, "setBackgroundColor", bg);
                String meta = courseMeta(item);
                v.setTextViewText(R.id.rowMeta, meta);
                v.setViewVisibility(R.id.rowMeta, meta.isEmpty() ? View.GONE : View.VISIBLE);
                v.setTextColor(R.id.rowTitle, ink);
                v.setTextColor(R.id.rowMeta, muted);
                v.setTextColor(R.id.rowRelative, darken(bg));
            }

            Intent fill = new Intent();
            fill.putExtra("open_mode", "week");
            v.setOnClickFillInIntent(R.id.rowRoot, fill);
            v.setOnClickFillInIntent(R.id.rowContent, fill);
            v.setOnClickFillInIntent(R.id.rowTitle, fill);
            v.setOnClickFillInIntent(R.id.rowMeta, fill);
            v.setOnClickFillInIntent(R.id.rowRelative, fill);
            return v;
        }

        private void applyBreakRow(RemoteViews v, Item item, int background, int ink, int pillInk) {
            v.setInt(R.id.rowContent, "setBackgroundColor", background);
            String meta = AdvancedSettingsStore.showTimes(context) ? item.time : "";
            v.setTextViewText(R.id.rowMeta, meta);
            v.setViewVisibility(R.id.rowMeta, meta.isEmpty() ? View.GONE : View.VISIBLE);
            v.setTextColor(R.id.rowTitle, ink);
            v.setTextColor(R.id.rowMeta, ink);
            v.setTextViewText(R.id.rowRelative, item.relative);
            v.setViewVisibility(R.id.rowRelative, item.relative.isEmpty() ? View.GONE : View.VISIBLE);
            v.setTextColor(R.id.rowRelative, pillInk);
        }

        private int darken(int color) {
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            r = Math.max(25, (int) (r * .58f));
            g = Math.max(25, (int) (g * .58f));
            b = Math.max(25, (int) (b * .58f));
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }
    }
}
