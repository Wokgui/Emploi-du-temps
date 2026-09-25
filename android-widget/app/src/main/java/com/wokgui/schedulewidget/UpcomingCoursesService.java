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

public class UpcomingCoursesService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent == null ? AppWidgetManager.INVALID_APPWIDGET_ID
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        int format = WidgetLayoutStore.get(getApplicationContext(), widgetId);
        if (format == WidgetLayoutStore.FORMAT_CONDENSED) {
            return CondensedCoursesService.createFactory(getApplicationContext(), widgetId);
        }
        if (format == WidgetLayoutStore.FORMAT_MINI) {
            return new MiniFactory(getApplicationContext(), widgetId);
        }
        return new Factory(getApplicationContext(), widgetId);
    }

    static List<RemoteViews> buildAdaptiveRows(Context context, int widgetId) {
        Factory factory = new Factory(context.getApplicationContext(), widgetId);
        factory.reload();
        List<RemoteViews> rows = new ArrayList<>();
        for (int i = 0; i < factory.items.size(); i++) rows.add(factory.createViewAt(i, true));
        return rows;
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
        private final Integer forcedHeightDp;
        private final Calendar forcedNow;
        private final List<Item> items = new ArrayList<>();
        private int widgetHeightDp = 120;

        Factory(Context context, int widgetId) {
            this(context, widgetId, null, null);
        }

        Factory(Context context, int widgetId, Integer forcedHeightDp, Calendar forcedNow) {
            this.context = context;
            this.widgetId = widgetId;
            this.forcedHeightDp = forcedHeightDp;
            this.forcedNow = forcedNow == null ? null : (Calendar) forcedNow.clone();
        }

        @Override public void onCreate() { reload(); }
        @Override public void onDataSetChanged() { reload(); }
        @Override public void onDestroy() { items.clear(); }
        @Override public int getCount() { return items.size(); }

        private int resolveWidgetHeightDp() {
            if (forcedHeightDp != null) return Math.max(1, forcedHeightDp);
            if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return 120;
            Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            if (options == null) return 120;
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            return WidgetHeightSizing.resolveHeightDp(
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0),
                    options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0),
                    landscape,
                    120
            );
        }

        private void reload() {
            items.clear();
            ScheduleStore.ensureInitialized(context);
            widgetHeightDp = resolveWidgetHeightDp();

            Calendar now = forcedNow == null ? Calendar.getInstance() : (Calendar) forcedNow.clone();
            int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            Target target = resolveTarget(now, nowMin);
            if (target == null) return;

            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, target.date);
            boolean automaticDensity = AdvancedSettingsStore.widgetAutoDensity(context);
            int firstCourse = automaticDensity ? 0 : target.firstCourse;
            if (courses == null || courses.isEmpty() || firstCourse >= courses.size()) return;

            int day = target.date.get(Calendar.DAY_OF_WEEK);
            int lunchStart = AdvancedSettingsStore.weekLunchStartMinute(context, day);
            int lunchEnd = AdvancedSettingsStore.weekLunchEndMinute(context, day);
            if (!AdvancedSettingsStore.weekLunchEnabled(context, day)) lunchEnd = lunchStart;
            boolean futureDay = !sameDay(now, target.date);
            int previousEnd = -1;
            boolean firstVisibleCourse = true;

            if (!automaticDensity && !futureDay && target.firstCourse > 0) {
                ScheduleData.Course previous = courses.get(target.firstCourse - 1);
                ScheduleData.Course next = courses.get(target.firstCourse);
                int from = ScheduleData.toMinutes(previous.end);
                int to = ScheduleData.toMinutes(next.start);
                if (nowMin >= from && nowMin < to) appendBreaks(from, to, lunchStart, lunchEnd, nowMin);
            }

            for (int i = firstCourse; i < courses.size(); i++) {
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
            appendBreaks(from, to, lunchStart, lunchEnd, -1);
        }

        private void appendBreaks(int from, int to, int lunchStart, int lunchEnd, int cutoffMinute) {
            for (WidgetBreakSequence.Segment segment : WidgetBreakSequence.between(
                    from, to, lunchStart, lunchEnd,
                    AdvancedSettingsStore.showBreaks(context),
                    AdvancedSettingsStore.showLunch(context), cutoffMinute)) {
                if (segment.type == WidgetBreakSequence.LUNCH) addLunch(segment.start, segment.end);
                else addGap(segment.start, segment.end, -1);
            }
        }

        private void addLunch(int start, int end) {
            String appLabel = localizedAppBreakLabel(true);
            String label = AdvancedSettingsStore.widgetLunchLabel(context, appLabel);
            items.add(new Item(label, appLabel, minuteLabel(start) + " - " + minuteLabel(end), "",
                    Item.LUNCH, 0, lunchRelative(end), false, ""));
        }

        private void addGap(int start, int end, int cutoffMinute) {
            if (!AdvancedSettingsStore.showBreaks(context) || end <= start) return;
            if (cutoffMinute >= 0 && end <= cutoffMinute) return;
            String appLabel = localizedAppBreakLabel(false);
            String label = AdvancedSettingsStore.widgetGapLabel(context, appLabel);
            items.add(new Item(label, appLabel, minuteLabel(start) + " - " + minuteLabel(end), "",
                    Item.GAP, 0, gapRelative(start, end), false, ""));
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
            else base = UiSettingsStore.t(context, "in") + " " + Math.max(1L, Math.round(diff / 60.0)) + " h";
            if (includeDate) return compactFutureLabel(targetDate, base);
            return base;
        }

        private String compactFutureLabel(Calendar date, String base) {
            String lang = UiSettingsStore.language(context);
            Locale locale = "de".equals(lang) ? Locale.GERMANY : ("en".equals(lang) ? Locale.UK : Locale.FRANCE);
            String day = new SimpleDateFormat("EEE d", locale).format(date.getTime()).replace(".", "");
            String prefix = UiSettingsStore.t(context, "in") + " ";
            String duration = base.startsWith(prefix) ? base.substring(prefix.length()) : base;
            return day + " · " + duration;
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
                return ", le " + d;
            }
            if ("de".equals(lang)) return ", am " + new SimpleDateFormat("EEEE, d. MMM.", locale).format(date.getTime());
            return ", " + new SimpleDateFormat("EEE d MMM", locale).format(date.getTime());
        }

        private boolean sameDay(Calendar a, Calendar b) {
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                    && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
        }

        private String minuteLabel(int minute) {
            return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60);
        }

        private String courseMeta(Item item) {
            boolean room = AdvancedSettingsStore.showRoom(context);
            if (room) return UiSettingsStore.t(context, "room") + " " + (item.room.isEmpty() ? "—" : item.room);
            return "";
        }

        private String startTime(String range) {
            int cut = range == null ? -1 : range.indexOf(" - ");
            return cut > 0 ? range.substring(0, cut) : (range == null ? "" : range);
        }

        @Override
        public RemoteViews getViewAt(int position) { return createViewAt(position, false); }

        private RemoteViews createViewAt(int position, boolean adaptiveHost) {
            if (position < 0 || position >= items.size()) return null;
            Item item = items.get(position);
            RemoteViews v = new RemoteViews(context.getPackageName(), adaptiveHost
                    ? R.layout.widget_adaptive_course_row : R.layout.widget_course_row);

            boolean automaticDensity = AdvancedSettingsStore.widgetAutoDensity(context);
            int sizingHeight = adaptiveHost
                    ? WidgetHeightSizing.adaptiveEstimateHeightDp(widgetHeightDp) : widgetHeightDp;
            int fittedHeight = automaticDensity
                    ? CondensedRowSizing.autoRowHeightDp(sizingHeight, items.size(), position, AdvancedSettingsStore.widgetBarChromeDp(context))
                    : 54;
            float scale = UiSettingsStore.widgetFontScale(context);
            float automaticScale = automaticDensity
                    ? WidgetAutoLayoutSizing.classicTextScale(fittedHeight)
                    : 1f;
            float pillScale = automaticDensity ? WidgetAutoLayoutSizing.pillTextScale(fittedHeight) : 1f;
            v.setTextViewTextSize(R.id.rowTitle, TypedValue.COMPLEX_UNIT_SP, 13f * scale * automaticScale);
            v.setTextViewTextSize(R.id.rowStartTime, TypedValue.COMPLEX_UNIT_SP, 10f * scale * automaticScale);
            v.setTextViewTextSize(R.id.rowMeta, TypedValue.COMPLEX_UNIT_SP, 10f * scale * automaticScale);
            v.setTextViewTextSize(R.id.rowRelative, TypedValue.COMPLEX_UNIT_SP, 8f * scale * pillScale);
            if (automaticDensity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (adaptiveHost) {
                    v.setViewLayoutHeight(R.id.adaptiveRowSlot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    v.setViewLayoutHeight(R.id.rowRoot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    v.setViewLayoutHeight(R.id.rowContent, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                } else {
                    v.setViewLayoutHeight(R.id.rowRoot, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                    v.setViewLayoutHeight(R.id.rowContent, fittedHeight, TypedValue.COMPLEX_UNIT_DIP);
                }
                v.setViewPadding(R.id.rowTextBlock, 0, 0, 0, 0);
                v.setViewLayoutHeight(R.id.rowRelative, WidgetAutoLayoutSizing.pillHeightDp(fittedHeight), TypedValue.COMPLEX_UNIT_DIP);
                v.setViewLayoutWidth(R.id.rowRelative, WidgetAutoLayoutSizing.pillWidthDp(fittedHeight), TypedValue.COMPLEX_UNIT_DIP);
                v.setViewLayoutWidth(R.id.rowRelativeBox, WidgetAutoLayoutSizing.pillBoxWidthDp(fittedHeight), TypedValue.COMPLEX_UNIT_DIP);
            }

            v.setViewVisibility(R.id.rowIndex, View.GONE);
            v.setViewVisibility(R.id.rowDot, View.GONE);
            v.setViewVisibility(R.id.rowLineTop, View.GONE);
            v.setViewVisibility(R.id.rowLineBottom, View.GONE);

            String title = item.label;
            if (item.type == Item.COURSE && item.uncertain) title = "⚠ " + title;
            v.setTextViewText(R.id.rowTitle, title);
            boolean showStartTime = AdvancedSettingsStore.showTimes(context);
            v.setTextViewText(R.id.rowStartTime, showStartTime ? startTime(item.time) : "");
            v.setViewVisibility(R.id.rowStartTime, showStartTime ? View.VISIBLE : View.GONE);
            String relative = AdvancedSettingsStore.showRemaining(context) ? item.relative : "";
            v.setTextViewText(R.id.rowRelative, relative);
            v.setViewVisibility(R.id.rowRelative, relative.isEmpty() ? View.GONE : View.VISIBLE);
            v.setInt(R.id.rowRelative, "setGravity", Gravity.CENTER);

            if (item.type == Item.LUNCH) {
                int bg = WidgetPaletteStore.lunchBackground(context);
                applyBreakRow(v, item, bg, WidgetPaletteStore.lunchText(context), darken(bg), relative);
            } else if (item.type == Item.GAP) {
                int bg = WidgetPaletteStore.gapBackground(context);
                int ink = WidgetPaletteStore.gapText(context);
                applyBreakRow(v, item, bg, ink, ink, relative);
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
                v.setTextColor(R.id.rowStartTime, ink);
                v.setTextColor(R.id.rowMeta, muted);
                v.setTextColor(R.id.rowRelative, darken(bg));
            }
            if (automaticDensity && !WidgetAutoLayoutSizing.showMeta(fittedHeight)) {
                v.setViewVisibility(R.id.rowMeta, View.GONE);
            }
            boolean showRelative = !relative.isEmpty()
                    && (!automaticDensity || WidgetAutoLayoutSizing.showPill(fittedHeight));
            v.setViewVisibility(R.id.rowRelative, showRelative ? View.VISIBLE : View.GONE);
            v.setViewVisibility(R.id.rowRelativeBox, showRelative ? View.VISIBLE : View.GONE);

            if (!adaptiveHost) {
                Intent fill = new Intent();
                fill.putExtra("open_mode", "week");
                v.setOnClickFillInIntent(R.id.rowRoot, fill);
                v.setOnClickFillInIntent(R.id.rowContent, fill);
                v.setOnClickFillInIntent(R.id.rowTitle, fill);
                v.setOnClickFillInIntent(R.id.rowStartTime, fill);
                v.setOnClickFillInIntent(R.id.rowMeta, fill);
                v.setOnClickFillInIntent(R.id.rowRelativeBox, fill);
                v.setOnClickFillInIntent(R.id.rowRelative, fill);
            }
            return v;
        }

        private void applyBreakRow(RemoteViews v, Item item, int background, int ink, int pillInk, String relative) {
            v.setInt(R.id.rowContent, "setBackgroundColor", background);
            v.setTextViewText(R.id.rowMeta, "");
            v.setViewVisibility(R.id.rowMeta, View.GONE);
            v.setTextColor(R.id.rowTitle, ink);
            v.setTextColor(R.id.rowStartTime, ink);
            v.setTextColor(R.id.rowMeta, ink);
            v.setTextViewText(R.id.rowRelative, relative);
            v.setViewVisibility(R.id.rowRelative, relative.isEmpty() ? View.GONE : View.VISIBLE);
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

    private static final class MiniFactory implements RemoteViewsFactory {
        private static final int[] CELL_IDS = {
                R.id.miniCell1, R.id.miniCell2, R.id.miniCell3, R.id.miniCell4,
                R.id.miniCell5, R.id.miniCell6, R.id.miniCell7, R.id.miniCell8,
                R.id.miniCell9, R.id.miniCell10, R.id.miniCell11
        };

        private static final class Segment {
            final int start;
            final String label;
            final String room;
            final int background;
            final int ink;
            final boolean course;
            final boolean visible;

            Segment(int start, String label, String room, int background, int ink, boolean course, boolean visible) {
                this.start = start;
                this.label = label == null ? "" : label;
                this.room = room == null ? "" : room;
                this.background = background;
                this.ink = ink;
                this.course = course;
                this.visible = visible;
            }
        }

        private final Context context;
        private final int widgetId;
        private final List<Segment> segments = new ArrayList<>();
        private Calendar targetDate;

        MiniFactory(Context context, int widgetId) {
            this.context = context;
            this.widgetId = widgetId;
        }

        @Override public void onCreate() { reload(); }
        @Override public void onDataSetChanged() { reload(); }
        @Override public void onDestroy() { segments.clear(); targetDate = null; }
        @Override public int getCount() { return targetDate == null ? 0 : 1; }

        private void reload() {
            segments.clear();
            ScheduleStore.ensureInitialized(context);
            targetDate = resolveTargetDate();
            if (targetDate == null) return;
            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, targetDate);
            if (courses == null || courses.isEmpty()) { targetDate = null; return; }

            for (int slot = 1; slot <= 4; slot++) addSlot(courses, slot);
            addBreak(courses, 4, 5, true);
            addSlot(courses, 5);
            addSlot(courses, 6);
            addBreak(courses, 6, 7, false);
            addSlot(courses, 7);
            addSlot(courses, 8);
            addSlot(courses, 9);

            int lastCourse = -1;
            for (int i = 0; i < segments.size(); i++) if (segments.get(i).course) lastCourse = i;
            for (int i = lastCourse + 1; i < segments.size(); i++) {
                Segment s = segments.get(i);
                segments.set(i, new Segment(s.start, s.label, s.room, s.background, s.ink, false, false));
            }
        }

        private Calendar resolveTargetDate() {
            Calendar now = Calendar.getInstance();
            int nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            List<ScheduleData.Course> today = ScheduleStore.getCourses(context, now);
            if (today != null && !today.isEmpty()) {
                int lastEnd = 0;
                for (ScheduleData.Course c : today) lastEnd = Math.max(lastEnd, ScheduleData.toMinutes(c.end));
                if (nowMinute < lastEnd) return (Calendar) now.clone();
            }
            Calendar cursor = (Calendar) now.clone();
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            for (int i = 0; i < 21; i++) {
                List<ScheduleData.Course> list = ScheduleStore.getCourses(context, cursor);
                if (list != null && !list.isEmpty()) return (Calendar) cursor.clone();
                cursor.add(Calendar.DAY_OF_YEAR, 1);
            }
            return null;
        }

        private void addSlot(List<ScheduleData.Course> courses, int slot) {
            int start = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, slot));
            int end = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, slot));
            segments.add(segment(courses, start, end, slot, false));
        }

        private void addBreak(List<ScheduleData.Course> courses, int beforeSlot, int afterSlot, boolean lunch) {
            int start = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, beforeSlot));
            int end = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, afterSlot));
            if (end <= start) {
                segments.add(new Segment(start, "", "", 0xFFF7F9FC, 0xFF64748B, false, false));
                return;
            }
            segments.add(segment(courses, start, end, 0, lunch));
        }

        private Segment segment(List<ScheduleData.Course> courses, int start, int end, int slot, boolean lunch) {
            ScheduleData.Course found = null;
            for (ScheduleData.Course c : courses) {
                if (slot > 0 && c.slot == slot) { found = c; break; }
            }
            if (found == null) {
                for (ScheduleData.Course c : courses) {
                    if (ScheduleData.toMinutes(c.start) < end && ScheduleData.toMinutes(c.end) > start) { found = c; break; }
                }
            }
            if (found != null) {
                int order = found.slot > 0 ? found.slot : Math.max(1, slot);
                int bg = WidgetPaletteStore.courseColor(context, order, found.label, found.color);
                boolean dark = WidgetPaletteStore.useDarkText(context, order, found.label, found.color);
                return new Segment(start, AdvancedSettingsStore.widgetCourseLabel(context, targetDate, found), found.room, bg,
                        dark ? 0xFF17213A : 0xFFFFFFFF, true, true);
            }
            if (lunch && AdvancedSettingsStore.showLunch(context)) {
                String label = ScheduleStore.getLunchLabel(context);
                if ("Pause de midi".equalsIgnoreCase(label)) label = "Midi";
                return new Segment(start, AdvancedSettingsStore.widgetLunchLabel(context, label), "",
                        WidgetPaletteStore.lunchBackground(context), WidgetPaletteStore.lunchText(context), false, true);
            }
            if (AdvancedSettingsStore.showBreaks(context)) {
                String label = ScheduleStore.getGapLabel(context);
                if ("Trou".equalsIgnoreCase(label)) label = UiSettingsStore.t(context, "gap");
                return new Segment(start, AdvancedSettingsStore.widgetGapLabel(context, label), "",
                        WidgetPaletteStore.gapBackground(context), WidgetPaletteStore.gapText(context), false, true);
            }
            return new Segment(start, "", "", 0xFFF7F9FC, 0xFF64748B, false, true);
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position != 0 || targetDate == null) return null;
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            v.setViewVisibility(R.id.rowContent, View.GONE);
            v.setViewVisibility(R.id.rowCondensedContent, View.GONE);
            v.setViewVisibility(R.id.rowMiniContent, View.VISIBLE);
            v.setTextViewText(R.id.rowMiniTitle, "Emploi du temps");

            float scale = UiSettingsStore.widgetFontScale(context);
            Bundle options = widgetId == AppWidgetManager.INVALID_APPWIDGET_ID ? null
                    : AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId);
            boolean landscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            int minWidth = options == null ? 0 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
            int maxWidth = options == null ? 0 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
            int widgetWidth = landscape ? Math.max(minWidth, maxWidth) : (minWidth > 0 ? minWidth : maxWidth);
            boolean narrowHeader = widgetWidth > 0 && widgetWidth <= 140;
            boolean compactHeader = widgetWidth > 0 && widgetWidth <= 190;
            v.setTextViewText(R.id.rowMiniDate, narrowHeader ? compactDateLabel(targetDate) : dateLabel(targetDate));
            v.setTextViewTextSize(R.id.rowMiniTitle, TypedValue.COMPLEX_UNIT_SP,
                    (narrowHeader ? 7f : (compactHeader ? 8f : 10f)) * scale);
            v.setTextViewTextSize(R.id.rowMiniDate, TypedValue.COMPLEX_UNIT_SP,
                    (narrowHeader ? 6f : (compactHeader ? 7f : 8f)) * scale);

            if (AdvancedSettingsStore.widgetAutoDensity(context)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                int minHeight = options == null ? 0 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
                int maxHeight = options == null ? 0 : options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);
                int contentHeight = WidgetHeightSizing.contentHeightDp(minHeight, maxHeight, landscape,
                        108, AdvancedSettingsStore.widgetBarChromeDp(context), 72);
                v.setViewLayoutHeight(R.id.rowRoot, contentHeight, TypedValue.COMPLEX_UNIT_DIP);
                v.setViewLayoutHeight(R.id.rowMiniContent, contentHeight, TypedValue.COMPLEX_UNIT_DIP);
                v.setViewLayoutHeight(R.id.rowMiniHeader, 20, TypedValue.COMPLEX_UNIT_DIP);
                v.setViewLayoutHeight(R.id.rowMiniTimeline, Math.max(52, contentHeight - 20), TypedValue.COMPLEX_UNIT_DIP);
            }

            for (int i = 0; i < CELL_IDS.length; i++) {
                int id = CELL_IDS[i];
                if (i >= segments.size() || !segments.get(i).visible) {
                    v.setViewVisibility(id, View.GONE);
                    continue;
                }
                Segment s = segments.get(i);
                v.setViewVisibility(id, View.VISIBLE);
                String text = hourLabel(s.start);
                if (!s.label.isEmpty()) text += "\n" + shortLabel(s.label);
                if (s.course && AdvancedSettingsStore.showRoom(context) && !s.room.isEmpty()) text += "\n" + shortRoom(s.room);
                v.setTextViewText(id, text);
                v.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, 7f * scale);
                v.setInt(id, "setBackgroundColor", s.background);
                v.setTextColor(id, s.ink);
            }

            Intent fill = new Intent();
            fill.putExtra("open_mode", "week");
            v.setOnClickFillInIntent(R.id.rowRoot, fill);
            v.setOnClickFillInIntent(R.id.rowMiniContent, fill);
            for (int id : CELL_IDS) v.setOnClickFillInIntent(id, fill);
            return v;
        }

        private String shortLabel(String value) {
            String text = value == null ? "" : value.trim();
            while (text.contains("  ")) text = text.replace("  ", " ");
            return text.length() <= 8 ? text : text.substring(0, 7).trim() + ".";
        }

        private String shortRoom(String value) {
            String text = value == null ? "" : value.trim();
            return text.length() <= 8 ? text : text.substring(0, 8).trim();
        }

        private String hourLabel(int minute) {
            if (minute < 0) return "";
            int h = minute / 60, m = minute % 60;
            return m == 0 ? h + "h" : String.format(Locale.FRANCE, "%d:%02d", h, m);
        }

        private String dateLabel(Calendar date) {
            String lang = UiSettingsStore.language(context);
            Locale locale = "de".equals(lang) ? Locale.GERMANY : ("en".equals(lang) ? Locale.UK : Locale.FRANCE);
            String pattern = "de".equals(lang) ? "EEE d. MMM" : "EEE d MMM";
            String label = new SimpleDateFormat(pattern, locale).format(date.getTime());
            return AdvancedSettingsStore.json(context).optBoolean("singleWeek", false)
                    ? label : label + " - " + ScheduleStore.getWeekLetter(context, date);
        }

        private String compactDateLabel(Calendar date) {
            String label = new SimpleDateFormat("dd/MM", Locale.FRANCE).format(date.getTime());
            return AdvancedSettingsStore.json(context).optBoolean("singleWeek", false)
                    ? label : label + " · " + ScheduleStore.getWeekLetter(context, date);
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return widgetId; }
        @Override public boolean hasStableIds() { return true; }
    }
}
