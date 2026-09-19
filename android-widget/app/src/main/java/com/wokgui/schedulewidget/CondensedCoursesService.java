package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
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

/** RemoteViews source for format 3: a compact chronological day list. */
public final class CondensedCoursesService extends RemoteViewsService {
    static RemoteViewsFactory createFactory(Context context, int widgetId) {
        return new Factory(context.getApplicationContext(), widgetId);
    }

    static List<RemoteViews> buildAdaptiveRows(Context context, int widgetId) {
        Factory factory = new Factory(context.getApplicationContext(), widgetId);
        factory.reload();
        List<RemoteViews> rows = new ArrayList<>();
        for (int i = 0; i < factory.items.size(); i++) rows.add(factory.createViewAt(i, true));
        return rows;
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
        final int progress;
        final boolean uncertain;

        Item(String label, String sourceLabel, String time, String room, int type, int order,
             int progress, String relative, boolean uncertain, String colorId) {
            this.label = label;
            this.sourceLabel = sourceLabel == null ? label : sourceLabel;
            this.time = time;
            this.room = room == null ? "" : room;
            this.type = type;
            this.order = order;
            this.progress = progress;
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
        private int widgetHeightDp = 180;

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
            boolean automaticDensity = AdvancedSettingsStore.widgetAutoDensity(context);
            int firstCourse = automaticDensity ? 0 : target.firstCourse;
            if (courses == null || courses.isEmpty() || firstCourse >= courses.size()) return;

            boolean futureDay = !sameDay(now, target.date);
            boolean firstVisibleCourse = true;
            int day = target.date.get(Calendar.DAY_OF_WEEK);
            int lunchStart = AdvancedSettingsStore.weekLunchStartMinute(context, day);
            int lunchEnd = AdvancedSettingsStore.weekLunchEndMinute(context, day);
            if (!AdvancedSettingsStore.weekLunchEnabled(context, day)) lunchEnd = lunchStart;
            int previousEnd = -1;

            if (!automaticDensity && !futureDay && target.firstCourse > 0) {
                ScheduleData.Course previous = courses.get(target.firstCourse - 1);
                ScheduleData.Course next = courses.get(target.firstCourse);
                int from = ScheduleData.toMinutes(previous.end);
                int to = ScheduleData.toMinutes(next.start);
                if (nowMin >= from && nowMin < to) appendBreaks(from, to, lunchStart, lunchEnd, nowMin);
            }

            for (int i = firstCourse; i < courses.size(); i++) {
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
                        courseProgress(now, target.date, start, ScheduleData.toMinutes(course.end)),
                        relativeLabel(now, target.date, course, futureDay && firstVisibleCourse),
                        course.uncertain,
                        course.color
                ));
                firstVisibleCourse = false;
                previousEnd = ScheduleData.toMinutes(course.end);
            }
        }

        private void appendBreaks(int from, int to, int lunchStart, int lunchEnd, int cutoffMinute) {
            for (WidgetBreakSequence.Segment segment : WidgetBreakSequence.between(
                    from, to, lunchStart, lunchEnd,
                    AdvancedSettingsStore.showBreaks(context),
                    AdvancedSettingsStore.showLunch(context), cutoffMinute)) {
                if (segment.type == WidgetBreakSequence.LUNCH) addBreak(Item.LUNCH, segment.start, segment.end);
                else addBreak(Item.GAP, segment.start, segment.end);
            }
        }

        private void addBreak(int type, int start, int end) {
            boolean lunch = type == Item.LUNCH;
            String appLabel = lunch ? ScheduleStore.getLunchLabel(context) : ScheduleStore.getGapLabel(context);
            if (lunch && "Pause de midi".equalsIgnoreCase(appLabel)) appLabel = UiSettingsStore.t(context, "lunch");
            if (!lunch && "Trou".equalsIgnoreCase(appLabel)) appLabel = UiSettingsStore.t(context, "gap");
            String label = lunch
                    ? AdvancedSettingsStore.widgetLunchLabel(context, appLabel)
                    : AdvancedSettingsStore.widgetGapLabel(context, appLabel);
            items.add(new Item(label, appLabel, minuteLabel(start) + " - " + minuteLabel(end), "",
                    type, 0, -1, "", false, ""));
        }

        private int courseProgress(Calendar now, Calendar date, int start, int end) {
            if (!sameDay(now, date) || end <= start) return -1;
            int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            if (minute < start || minute >= end) return -1;
            return Math.max(0, Math.min(1000, Math.round((minute - start) * 1000f / (end - start))));
        }

        private String minuteLabel(int minute) {
            return String.format(Locale.US, "%02d:%02d", minute / 60, minute % 60);
        }

        private int resolveWidgetHeightDp() {
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return 180;
            Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            if (options == null) return 180;
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            return WidgetHeightSizing.resolveHeightDp(
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
                    landscape,
                    180
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
        public RemoteViews getViewAt(int position) { return createViewAt(position, false); }

        private RemoteViews createViewAt(int position, boolean adaptiveHost) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews views = new RemoteViews(context.getPackageName(), adaptiveHost
                    ? R.layout.widget_adaptive_course_row : R.layout.widget_course_row);

            int densityPercent = AdvancedSettingsStore.widgetDensityPercent(context);
            boolean automaticDensity = AdvancedSettingsStore.widgetAutoDensity(context);
            int sizingHeight = adaptiveHost
                    ? WidgetHeightSizing.adaptiveEstimateHeightDp(widgetHeightDp) : widgetHeightDp;
            int fittedHeight = automaticDensity
                    ? CondensedRowSizing.autoRowHeightDp(sizingHeight, items.size(), position,
                            AdvancedSettingsStore.widgetBarChromeDp(context))
                    : CondensedRowSizing.rowHeightForPercent(densityPercent);
            float scale = UiSettingsStore.widgetFontScale(context);
            float densityScale = CondensedRowSizing.textScaleForRow(fittedHeight);
            views.setTextViewTextSize(R.id.rowCondensedTime, TypedValue.COMPLEX_UNIT_SP, 7f * scale * densityScale);
            views.setTextViewTextSize(R.id.rowCondensedTitle, TypedValue.COMPLEX_UNIT_SP, 9f * scale * densityScale);

            views.setViewVisibility(R.id.rowContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedContent, View.VISIBLE);
            views.setViewVisibility(R.id.rowMiniContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedLineTop, position == 0 ? View.GONE : View.VISIBLE);
            views.setViewVisibility(R.id.rowCondensedLineBottom, position == items.size() - 1 ? View.GONE : View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (adaptiveHost) {
                    views.setViewLayoutHeight(R.id.adaptiveRowSlot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    views.setViewLayoutHeight(R.id.rowRoot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    views.setViewLayoutHeight(R.id.rowCondensedContent, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                } else {
                    views.setViewLayoutHeight(R.id.rowRoot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    views.setViewLayoutHeight(R.id.rowCondensedContent, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                }
                int halfLine = Math.max(1, (fittedHeight + 1) / 2);
                views.setViewLayoutHeight(R.id.rowCondensedLineTop, halfLine, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedLineBottom, halfLine, TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedAccent, Math.max(1, fittedHeight - 3), TypedValue.COMPLEX_UNIT_DIP);
                views.setViewLayoutHeight(R.id.rowCondensedCourseProgress, Math.max(1, Math.min(5, fittedHeight - 1)), TypedValue.COMPLEX_UNIT_DIP);
            }

            boolean showCourseProgress = item.progress >= 0 && AdvancedSettingsStore.showProgress(context);
            views.setViewVisibility(R.id.rowCondensedCourseProgress, showCourseProgress ? View.VISIBLE : View.GONE);
            if (showCourseProgress) views.setProgressBar(R.id.rowCondensedCourseProgress, 1000, item.progress, false);

            String title = item.label;
            if (item.uncertain) title = "⚠ " + title;
            String meta = item.type == Item.COURSE ? condensedMeta(item) : "";
            if (!meta.isEmpty()) title += " · " + meta;
            views.setTextViewText(R.id.rowCondensedTitle, title);
            views.setTextViewText(R.id.rowCondensedTime, AdvancedSettingsStore.showTimes(context) ? startTime(item.time) : "");
            views.setTextViewText(R.id.rowCondensedMeta, "");
            views.setViewVisibility(R.id.rowCondensedMeta, View.GONE);

            int accent;
            if (item.type != Item.COURSE) {
                int background = item.type == Item.LUNCH
                        ? WidgetPaletteStore.lunchBackground(context) : WidgetPaletteStore.gapBackground(context);
                int text = item.type == Item.LUNCH
                        ? WidgetPaletteStore.lunchText(context) : WidgetPaletteStore.gapText(context);
                views.setInt(R.id.rowCondensedContent, "setBackgroundColor", background);
                views.setTextColor(R.id.rowCondensedTime, text);
                views.setTextColor(R.id.rowCondensedTitle, text);
                views.setInt(R.id.rowCondensedTitle, "setGravity", Gravity.CENTER);
                accent = text;
            } else {
                views.setInt(R.id.rowCondensedContent, "setBackgroundColor", 0x00FFFFFF);
                views.setTextColor(R.id.rowCondensedTime, 0xFF5D6B82);
                views.setTextColor(R.id.rowCondensedTitle, 0xFF17213A);
                views.setInt(R.id.rowCondensedTitle, "setGravity", Gravity.START | Gravity.CENTER_VERTICAL);
                accent = WidgetPaletteStore.courseColor(context, item.order, item.sourceLabel, item.colorId);
            }
            views.setInt(R.id.rowCondensedAccent, "setBackgroundColor", accent);

            if (!adaptiveHost) {
                Intent fill = new Intent();
                fill.putExtra("open_mode", "week");
                views.setOnClickFillInIntent(R.id.rowRoot, fill);
                views.setOnClickFillInIntent(R.id.rowCondensedContent, fill);
                views.setOnClickFillInIntent(R.id.rowCondensedTime, fill);
                views.setOnClickFillInIntent(R.id.rowCondensedTitle, fill);
                views.setOnClickFillInIntent(R.id.rowCondensedMeta, fill);
            }
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
