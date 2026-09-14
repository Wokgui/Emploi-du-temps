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

/** RemoteViews source for format 4: one horizontal mini-planning row. */
public final class MiniPlanningService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent == null ? AppWidgetManager.INVALID_APPWIDGET_ID
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new Factory(getApplicationContext(), widgetId);
    }

    private static final class Segment {
        final int startMinute;
        final String label;
        final int background;
        final int ink;
        final boolean course;
        final boolean visible;

        Segment(int startMinute, String label, int background, int ink, boolean course, boolean visible) {
            this.startMinute = startMinute;
            this.label = label == null ? "" : label;
            this.background = background;
            this.ink = ink;
            this.course = course;
            this.visible = visible;
        }
    }

    private static final class Factory implements RemoteViewsFactory {
        private static final int[] CELL_IDS = {
                R.id.miniCell1, R.id.miniCell2, R.id.miniCell3, R.id.miniCell4,
                R.id.miniCell5, R.id.miniCell6, R.id.miniCell7, R.id.miniCell8,
                R.id.miniCell9, R.id.miniCell10, R.id.miniCell11
        };

        private final Context context;
        private final int widgetId;
        private final List<Segment> segments = new ArrayList<>();
        private Calendar targetDate;

        Factory(Context context, int widgetId) {
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
            if (courses == null || courses.isEmpty()) {
                targetDate = null;
                return;
            }

            for (int slot = 1; slot <= 4; slot++) addSlot(courses, slot);
            addBreak(courses,
                    ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4)),
                    ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5)),
                    true);
            addSlot(courses, 5);
            addSlot(courses, 6);
            addBreak(courses,
                    ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 6)),
                    ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 7)),
                    false);
            addSlot(courses, 7);
            addSlot(courses, 8);
            addSlot(courses, 9);

            int lastCourse = -1;
            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i).course) lastCourse = i;
            }
            if (lastCourse >= 0) {
                for (int i = lastCourse + 1; i < segments.size(); i++) {
                    Segment s = segments.get(i);
                    segments.set(i, new Segment(s.startMinute, s.label, s.background, s.ink, false, false));
                }
            }
        }

        private Calendar resolveTargetDate() {
            Calendar now = Calendar.getInstance();
            int nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            List<ScheduleData.Course> today = ScheduleStore.getCourses(context, now);
            if (today != null && !today.isEmpty()) {
                int lastEnd = 0;
                for (ScheduleData.Course course : today) {
                    lastEnd = Math.max(lastEnd, ScheduleData.toMinutes(course.end));
                }
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
            segments.add(segmentForInterval(courses, start, end, slot, false));
        }

        private void addBreak(List<ScheduleData.Course> courses, int start, int end, boolean lunch) {
            if (end <= start) {
                segments.add(new Segment(start, "", 0x00000000, 0xFF64748B, false, false));
                return;
            }
            segments.add(segmentForInterval(courses, start, end, 0, lunch));
        }

        private Segment segmentForInterval(List<ScheduleData.Course> courses, int start, int end, int slot, boolean lunch) {
            ScheduleData.Course found = null;
            for (ScheduleData.Course course : courses) {
                if (slot > 0 && course.slot == slot) {
                    found = course;
                    break;
                }
            }
            if (found == null) {
                for (ScheduleData.Course course : courses) {
                    int cStart = ScheduleData.toMinutes(course.start);
                    int cEnd = ScheduleData.toMinutes(course.end);
                    if (cStart < end && cEnd > start) {
                        found = course;
                        break;
                    }
                }
            }

            if (found != null) {
                int order = found.slot > 0 ? found.slot : Math.max(1, slot);
                String label = AdvancedSettingsStore.widgetCourseLabel(context, targetDate, found);
                int background = WidgetPaletteStore.courseColor(context, order, found.label, found.color);
                boolean dark = WidgetPaletteStore.useDarkText(context, order, found.label, found.color);
                return new Segment(start, label, background, dark ? 0xFF17213A : 0xFFFFFFFF, true, true);
            }

            if (lunch && AdvancedSettingsStore.showLunch(context)) {
                String appLabel = ScheduleStore.getLunchLabel(context);
                if ("Pause de midi".equalsIgnoreCase(appLabel)) appLabel = "Midi";
                return new Segment(
                        start,
                        AdvancedSettingsStore.widgetLunchLabel(context, appLabel),
                        WidgetPaletteStore.lunchBackground(context),
                        WidgetPaletteStore.lunchText(context),
                        false,
                        true
                );
            }

            if (AdvancedSettingsStore.showBreaks(context)) {
                String appLabel = ScheduleStore.getGapLabel(context);
                if ("Trou".equalsIgnoreCase(appLabel)) appLabel = UiSettingsStore.t(context, "gap");
                return new Segment(
                        start,
                        AdvancedSettingsStore.widgetGapLabel(context, appLabel),
                        WidgetPaletteStore.gapBackground(context),
                        WidgetPaletteStore.gapText(context),
                        false,
                        true
                );
            }

            return new Segment(start, "", 0x00000000, 0xFF64748B, false, true);
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position != 0 || targetDate == null) return null;
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_course_row);
            views.setViewVisibility(R.id.rowContent, View.GONE);
            views.setViewVisibility(R.id.rowCondensedContent, View.GONE);
            views.setViewVisibility(R.id.rowMiniContent, View.VISIBLE);

            views.setTextViewText(R.id.rowMiniTitle, "Emploi du temps");
            views.setTextViewText(R.id.rowMiniDate, dateLabel(targetDate));

            for (int i = 0; i < CELL_IDS.length; i++) {
                int id = CELL_IDS[i];
                if (i >= segments.size() || !segments.get(i).visible) {
                    views.setViewVisibility(id, View.GONE);
                    continue;
                }
                Segment segment = segments.get(i);
                views.setViewVisibility(id, View.VISIBLE);
                views.setTextViewText(id, cellText(segment));
                views.setInt(id, "setBackgroundColor", segment.background);
                views.setTextColor(id, segment.ink);
            }

            Intent fill = new Intent();
            fill.putExtra("open_mode", "week");
            views.setOnClickFillInIntent(R.id.rowRoot, fill);
            views.setOnClickFillInIntent(R.id.rowMiniContent, fill);
            views.setOnClickFillInIntent(R.id.rowMiniTitle, fill);
            views.setOnClickFillInIntent(R.id.rowMiniDate, fill);
            for (int id : CELL_IDS) views.setOnClickFillInIntent(id, fill);
            return views;
        }

        private String cellText(Segment segment) {
            String time = hourLabel(segment.startMinute);
            if (segment.label.isEmpty()) return time;
            return time + "\n" + shortLabel(segment.label);
        }

        private String shortLabel(String value) {
            String text = value == null ? "" : value.trim();
            while (text.contains("  ")) text = text.replace("  ", " ");
            if (text.length() <= 8) return text;
            return text.substring(0, 7).trim() + ".";
        }

        private String hourLabel(int minute) {
            if (minute < 0) return "";
            int h = minute / 60;
            int m = minute % 60;
            return m == 0 ? h + "h" : String.format(Locale.FRANCE, "%d:%02d", h, m);
        }

        private String dateLabel(Calendar date) {
            String lang = UiSettingsStore.language(context);
            Locale locale = "de".equals(lang) ? Locale.GERMANY : ("en".equals(lang) ? Locale.UK : Locale.FRANCE);
            String pattern = "de".equals(lang) ? "EEE d. MMM" : "EEE d MMM";
            String label = new SimpleDateFormat(pattern, locale).format(date.getTime());
            return label + " - " + ScheduleStore.getWeekLetter(context, date);
        }

        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return widgetId; }
        @Override public boolean hasStableIds() { return true; }
    }
}
