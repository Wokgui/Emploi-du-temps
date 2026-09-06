package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.wokgui.schedulewidget.REFRESH";
    static final String ACTION_BOUNDARY = "com.wokgui.schedulewidget.BOUNDARY";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        ScheduleStore.ensureInitialized(context);
        for (int id : appWidgetIds) updateWidget(context, manager, id);
        scheduleNextBoundary(context);
        ReminderScheduler.reschedule(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action)
                || ACTION_BOUNDARY.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)) {
            updateAll(context);
            scheduleNextBoundary(context);
            ReminderScheduler.reschedule(context);
        }
    }

    @Override public void onEnabled(Context context) {
        ScheduleStore.ensureInitialized(context);
        updateAll(context);
        scheduleNextBoundary(context);
        ReminderScheduler.reschedule(context);
    }

    @Override public void onDisabled(Context context) { cancelBoundary(context); }
    static void refreshAll(Context context) { updateAll(context); }

    private static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScheduleWidgetProvider.class));
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        ScheduleStore.ensureInitialized(context);
        Calendar now = Calendar.getInstance();
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, now);

        ScheduleData.Course current = null;
        for (ScheduleData.Course c : courses) {
            int s = ScheduleData.toMinutes(c.start), e = ScheduleData.toMinutes(c.end);
            if (minute >= s && minute < e) { current = c; break; }
        }

        int lunchStart = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4));
        int lunchEnd = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5));
        boolean lunchValid = lunchEnd > lunchStart;
        boolean inLunch = AdvancedSettingsStore.showLunch(context) && current == null && !courses.isEmpty() && lunchValid
                && minute >= lunchStart && minute < lunchEnd;
        GapInfo gap = current == null && !inLunch && AdvancedSettingsStore.showBreaks(context)
                ? findCurrentGap(courses, minute, lunchStart, lunchEnd) : null;
        NextCourseInfo next = findNextCourse(context, now);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        applyAppearance(context, views);

        Calendar tileDate = (current == null && !inLunch && gap == null && next != null) ? next.date : now;
        views.setTextViewText(R.id.tvTileDate, formatWidgetDate(context, tileDate));
        views.setViewVisibility(R.id.tvKindActive, View.GONE);
        views.setViewVisibility(R.id.tvKind, View.VISIBLE);
        views.setViewVisibility(R.id.tvRemaining, View.GONE);
        views.setViewVisibility(R.id.tvProgressPercent, View.GONE);
        views.setViewVisibility(R.id.classProgress, AdvancedSettingsStore.showProgress(context) ? View.VISIBLE : View.GONE);

        String accentLabel = null;
        if (current != null) {
            accentLabel = current.label;
            views.setViewVisibility(R.id.tvKind, View.GONE);
            views.setViewVisibility(R.id.tvKindActive, View.VISIBLE);
            views.setTextViewText(R.id.tvKindActive, UiSettingsStore.t(context, "current"));
            views.setTextViewText(R.id.tvStatus, displayLabel(current));
            views.setTextViewText(R.id.tvSubstatus, courseMeta(context, current));
            views.setViewVisibility(R.id.tvSubstatus, courseMeta(context, current).isEmpty() ? View.GONE : View.VISIBLE);
        } else if (inLunch) {
            String start = ScheduleStore.getSlotEnd(context, 4);
            String end = ScheduleStore.getSlotStart(context, 5);
            views.setViewVisibility(R.id.tvKind, View.GONE);
            views.setTextViewText(R.id.tvStatus, localizedBreakLabel(context, true));
            String meta = AdvancedSettingsStore.showTimes(context) ? start + " - " + end + " · " : "";
            meta += UiSettingsStore.t(context, "backAt") + " " + end;
            views.setTextViewText(R.id.tvSubstatus, meta);
        } else if (gap != null) {
            views.setViewVisibility(R.id.tvKind, View.GONE);
            views.setTextViewText(R.id.tvStatus, localizedBreakLabel(context, false));
            String meta = AdvancedSettingsStore.showTimes(context) ? minuteLabel(gap.start) + " - " + minuteLabel(gap.end) + " · " : "";
            meta += durationLabel(gap.end - gap.start);
            views.setTextViewText(R.id.tvSubstatus, meta);
        } else if (next != null) {
            accentLabel = next.course.label;
            views.setTextViewText(R.id.tvKind, UiSettingsStore.t(context, "next"));
            views.setTextViewText(R.id.tvStatus, displayLabel(next.course));
            String meta = courseMeta(context, next.course);
            views.setTextViewText(R.id.tvSubstatus, meta);
            views.setViewVisibility(R.id.tvSubstatus, meta.isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.tvKind, View.GONE);
            views.setTextViewText(R.id.tvStatus, UiSettingsStore.t(context, "noCourse"));
            views.setTextViewText(R.id.tvSubstatus, "");
            views.setViewVisibility(R.id.tvSubstatus, View.GONE);
        }

        if (accentLabel != null) applyCourseAccent(context, views, accentLabel);

        if (current != null) {
            int progress = courseProgress(current, minute);
            int remaining = Math.max(0, ScheduleData.toMinutes(current.end) - minute);
            views.setProgressBar(R.id.classProgress, 100, progress, false);
            if (AdvancedSettingsStore.showPercent(context) && AdvancedSettingsStore.showProgress(context)) {
                views.setViewVisibility(R.id.tvProgressPercent, View.VISIBLE);
                views.setTextViewText(R.id.tvProgressPercent, progress + "%");
            }
            if (AdvancedSettingsStore.showRemaining(context)) {
                views.setViewVisibility(R.id.tvRemaining, View.VISIBLE);
                views.setTextViewText(R.id.tvRemaining, remainingLabel(context, remaining));
            }
        } else {
            views.setProgressBar(R.id.classProgress, 100, 100, false);
            views.setViewVisibility(R.id.tvProgressPercent, View.GONE);
            views.setViewVisibility(R.id.tvRemaining, View.GONE);
        }

        Intent listIntent = new Intent(context, UpcomingCoursesService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.upcomingList, listIntent);
        views.setEmptyView(R.id.upcomingList, R.id.emptyUpcoming);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("open_mode", "today");
        PendingIntent openPending = PendingIntent.getActivity(context, 100 + widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);

        Intent editIntent = new Intent(context, MainActivity.class);
        editIntent.putExtra("open_mode", "edit");
        PendingIntent editPending = PendingIntent.getActivity(context, 200 + widgetId, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.upcomingList, editPending);

        manager.updateAppWidget(widgetId, views);
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.upcomingList);
    }

    private static String displayLabel(ScheduleData.Course course) {
        return (course.uncertain ? "⚠ " : "") + course.label;
    }

    private static String courseMeta(Context context, ScheduleData.Course course) {
        boolean times = AdvancedSettingsStore.showTimes(context);
        boolean room = AdvancedSettingsStore.showRoom(context);
        if (times && room) return course.start + " - " + course.end + " · " + UiSettingsStore.t(context, "room") + " " + room(course.room);
        if (times) return course.start + " - " + course.end;
        if (room) return UiSettingsStore.t(context, "room") + " " + room(course.room);
        return "";
    }

    private static void applyAppearance(Context context, RemoteViews views) {
        UiSettingsStore.Theme theme = UiSettingsStore.theme(context);
        float scale = UiSettingsStore.widgetFontScale(context);
        int surface = theme.widgetSurface, card = theme.widgetCard, ink = theme.ink, muted = theme.muted, accent = theme.accent, track = theme.progressTrack;
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) {
            surface = 0xFFFFFFFF; card = 0xFFFFFFFF; ink = 0xFF000000; muted = 0xFF303030; accent = 0xFF0057B8; track = 0xFFBFC7D1;
        }
        views.setInt(R.id.widgetRoot, "setBackgroundColor", surface);
        views.setInt(R.id.currentCard, "setBackgroundColor", card);
        views.setInt(R.id.tvKind, "setBackgroundColor", accent);
        views.setInt(R.id.tvKindActive, "setBackgroundColor", accent);
        views.setTextColor(R.id.tvStatus, ink);
        views.setTextColor(R.id.tvSubstatus, muted);
        views.setTextColor(R.id.tvTileDate, muted);
        views.setTextColor(R.id.tvRemaining, muted);
        views.setTextColor(R.id.tvProgressPercent, accent);
        views.setTextColor(R.id.emptyUpcoming, muted);

        String density = AdvancedSettingsStore.density(context);
        float densityScale = "compact".equals(density) ? .94f : ("comfortable".equals(density) ? 1.08f : 1f);
        views.setTextViewTextSize(R.id.tvKind, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvKindActive, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvStatus, TypedValue.COMPLEX_UNIT_SP, 19f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvSubstatus, TypedValue.COMPLEX_UNIT_SP, 11f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvTileDate, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvRemaining, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvProgressPercent, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.emptyUpcoming, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setColorStateList(R.id.classProgress, "setProgressTintList", ColorStateList.valueOf(accent));
            views.setColorStateList(R.id.classProgress, "setProgressBackgroundTintList", ColorStateList.valueOf(track));
        }
    }

    private static void applyCourseAccent(Context context, RemoteViews views, String label) {
        UiSettingsStore.Theme theme = UiSettingsStore.theme(context);
        int accent = AdvancedSettingsStore.classColor(context, label, theme.accent);
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) accent = 0xFF0057B8;
        views.setInt(R.id.tvKind, "setBackgroundColor", accent);
        views.setInt(R.id.tvKindActive, "setBackgroundColor", accent);
        views.setTextColor(R.id.tvProgressPercent, accent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setColorStateList(R.id.classProgress, "setProgressTintList", ColorStateList.valueOf(accent));
        }
    }

    private static String localizedBreakLabel(Context context, boolean lunch) {
        String custom = lunch ? ScheduleStore.getLunchLabel(context) : ScheduleStore.getGapLabel(context);
        if (lunch && "Pause de midi".equalsIgnoreCase(custom)) return UiSettingsStore.t(context, "lunch");
        if (!lunch && "Trou".equalsIgnoreCase(custom)) return UiSettingsStore.t(context, "gap");
        return custom;
    }

    private static GapInfo findCurrentGap(List<ScheduleData.Course> courses, int minute, int lunchStart, int lunchEnd) {
        if (courses == null || courses.size() < 2) return null;
        ScheduleData.Course previous = null, next = null;
        int previousEnd = -1, nextStart = Integer.MAX_VALUE;
        for (ScheduleData.Course c : courses) {
            int start = ScheduleData.toMinutes(c.start), end = ScheduleData.toMinutes(c.end);
            if (end <= minute && end > previousEnd) { previous = c; previousEnd = end; }
            if (start > minute && start < nextStart) { next = c; nextStart = start; }
        }
        if (previous == null || next == null || nextStart <= previousEnd || minute < previousEnd || minute >= nextStart) return null;
        boolean lunchValid = lunchEnd > lunchStart;
        if (lunchValid && minute >= lunchStart && minute < lunchEnd) return null;
        int start = previousEnd, end = nextStart;
        if (lunchValid) {
            if (minute < lunchStart && end > lunchStart) end = lunchStart;
            else if (minute >= lunchEnd && start < lunchEnd) start = lunchEnd;
        }
        if (end <= start || minute < start || minute >= end) return null;
        return new GapInfo(start, end, next);
    }

    private static int courseProgress(ScheduleData.Course course, int minute) {
        if (course == null) return 0;
        int start = ScheduleData.toMinutes(course.start), end = ScheduleData.toMinutes(course.end);
        int duration = end - start;
        if (duration <= 0 || minute <= start) return 0;
        if (minute >= end) return 100;
        return clamp((int) Math.round((minute - start) * 100.0 / duration));
    }

    private static NextCourseInfo findNextCourse(Context context, Calendar now) {
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        Calendar cursor = (Calendar) now.clone();
        for (int add = 0; add < 21; add++) {
            List<ScheduleData.Course> list = ScheduleStore.getCourses(context, cursor);
            for (ScheduleData.Course c : list) {
                if (add == 0 && ScheduleData.toMinutes(c.start) <= nowMin) continue;
                return new NextCourseInfo((Calendar) cursor.clone(), c);
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return null;
    }

    private static String formatWidgetDate(Context context, Calendar date) {
        Locale locale = UiSettingsStore.locale(context);
        String label = new SimpleDateFormat("EEEE d MMMM", locale).format(date.getTime());
        if (!label.isEmpty()) label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        if (!AdvancedSettingsStore.showWeekInfo(context)) return label;
        return label + " · S" + date.get(Calendar.WEEK_OF_YEAR) + " · " + ScheduleStore.getWeekLetter(context, date);
    }

    private static String remainingLabel(Context context, int minutes) {
        if (minutes <= 0) return UiSettingsStore.t(context, "courseEnd");
        int h = minutes / 60, m = minutes % 60;
        String lang = UiSettingsStore.language(context);
        if ("de".equals(lang)) {
            if (h > 0 && m > 0) return h + " Std. " + m + " Min. übrig";
            if (h > 0) return h + " " + UiSettingsStore.t(context, h == 1 ? "remainingHour" : "remainingHours");
            return m + " " + UiSettingsStore.t(context, m == 1 ? "remainingMinute" : "remainingMinutes");
        }
        if ("en".equals(lang)) {
            if (h > 0 && m > 0) return h + " h " + m + " min left";
            if (h > 0) return h + " " + UiSettingsStore.t(context, h == 1 ? "remainingHour" : "remainingHours");
            return m + " " + UiSettingsStore.t(context, m == 1 ? "remainingMinute" : "remainingMinutes");
        }
        if (h > 0 && m > 0) return h + " h " + m + " min restantes";
        if (h > 0) return h + " " + UiSettingsStore.t(context, h == 1 ? "remainingHour" : "remainingHours");
        return m + " " + UiSettingsStore.t(context, m == 1 ? "remainingMinute" : "remainingMinutes");
    }

    private static String minuteLabel(int minute) { return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60); }
    private static String durationLabel(int minutes) {
        if (minutes <= 0) return "";
        int h = minutes / 60, m = minutes % 60;
        if (h > 0 && m > 0) return h + " h " + m;
        if (h > 0) return h + " h";
        return m + " min";
    }
    private static String room(String room) { return room == null || room.trim().isEmpty() ? "—" : room; }
    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }

    private static void scheduleNextBoundary(Context context) {
        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis(), targetMs = Long.MAX_VALUE;
        int nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> todayCourses = ScheduleStore.getCourses(context, now);
        for (ScheduleData.Course c : todayCourses) {
            int start = ScheduleData.toMinutes(c.start), end = ScheduleData.toMinutes(c.end);
            if (nowMinute >= start && nowMinute < end) {
                Calendar tick = (Calendar) now.clone();
                tick.add(Calendar.MINUTE, 1); tick.set(Calendar.SECOND, 2); tick.set(Calendar.MILLISECOND, 0);
                targetMs = tick.getTimeInMillis(); break;
            }
        }
        Calendar day = (Calendar) now.clone();
        for (int add = 0; add <= 21; add++) {
            List<Integer> boundaries = ScheduleData.boundaries(ScheduleStore.getCourses(context, day));
            boundaries.add(ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4)));
            boundaries.add(ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5)));
            for (int boundaryMin : boundaries) {
                Calendar candidate = (Calendar) day.clone();
                candidate.set(Calendar.HOUR_OF_DAY, boundaryMin / 60); candidate.set(Calendar.MINUTE, boundaryMin % 60);
                candidate.set(Calendar.SECOND, 2); candidate.set(Calendar.MILLISECOND, 0);
                long candidateMs = candidate.getTimeInMillis();
                if (candidateMs > nowMs + 1000 && candidateMs < targetMs) targetMs = candidateMs;
            }
            day.add(Calendar.DAY_OF_YEAR, 1); day.set(Calendar.HOUR_OF_DAY, 0); day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0); day.set(Calendar.MILLISECOND, 0);
        }
        if (targetMs == Long.MAX_VALUE) return;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMs, pending);
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pending);
    }

    private static class GapInfo {
        final int start, end; final ScheduleData.Course next;
        GapInfo(int start, int end, ScheduleData.Course next) { this.start=start; this.end=end; this.next=next; }
    }
    private static class NextCourseInfo {
        final Calendar date; final ScheduleData.Course course;
        NextCourseInfo(Calendar date, ScheduleData.Course course) { this.date=date; this.course=course; }
    }
}
