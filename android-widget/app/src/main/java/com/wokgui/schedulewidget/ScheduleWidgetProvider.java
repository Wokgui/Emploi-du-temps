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
import android.os.Bundle;
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
    static final String ACTION_TOGGLE_MODE = "com.wokgui.schedulewidget.TOGGLE_MODE";

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
        if (ACTION_TOGGLE_MODE.equals(action)) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                WidgetModeStore.toggle(context, id);
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                manager.notifyAppWidgetViewDataChanged(id, R.id.upcomingList);
                updateWidget(context, manager, id);
            }
            return;
        }
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

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int appWidgetId, Bundle newOptions) {
        updateWidget(context, manager, appWidgetId);
    }

    @Override public void onDeleted(Context context, int[] appWidgetIds) {
        for (int id : appWidgetIds) WidgetModeStore.clear(context, id);
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
        boolean dayMode = WidgetModeStore.isDayMode(context, widgetId);

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
        views.setTextViewText(R.id.btnWidgetMode, dayMode ? localizedTwoCourses(context) : localizedDay(context));

        Calendar tileDate = dayMode ? now : ((current == null && !inLunch && gap == null && next != null) ? next.date : now);
        views.setTextViewText(R.id.tvTileDate, formatWidgetDate(context, tileDate));
        views.setViewVisibility(R.id.currentCard, dayMode ? View.GONE : View.VISIBLE);
        views.setViewVisibility(R.id.tvKindActive, View.GONE);
        views.setViewVisibility(R.id.tvKind, View.VISIBLE);
        views.setViewVisibility(R.id.tvRemaining, View.GONE);
        views.setViewVisibility(R.id.tvProgressPercent, View.GONE);
        views.setViewVisibility(R.id.classProgress, View.GONE);

        if (!dayMode) {
            if (current != null) {
                applyHeroColor(views, current.slot, current.label);
                views.setViewVisibility(R.id.tvKind, View.GONE);
                views.setViewVisibility(R.id.tvKindActive, View.VISIBLE);
                views.setTextViewText(R.id.tvKindActive, UiSettingsStore.t(context, "current"));
                views.setTextViewText(R.id.tvStatus, displayLabel(current));
                String meta = courseMeta(context, current);
                views.setTextViewText(R.id.tvSubstatus, meta);
                views.setViewVisibility(R.id.tvSubstatus, meta.isEmpty() ? View.GONE : View.VISIBLE);
                int remaining = Math.max(1, ScheduleData.toMinutes(current.end) - minute);
                views.setViewVisibility(R.id.tvRemaining, View.VISIBLE);
                views.setTextViewText(R.id.tvRemaining, shortMinutes(context, remaining));
            } else if (inLunch) {
                applyBreakHero(views, true);
                String start = ScheduleStore.getSlotEnd(context, 4);
                String end = ScheduleStore.getSlotStart(context, 5);
                views.setViewVisibility(R.id.tvKind, View.GONE);
                views.setTextViewText(R.id.tvStatus, localizedBreakLabel(context, true));
                String meta = AdvancedSettingsStore.showTimes(context) ? start + " - " + end : "";
                views.setTextViewText(R.id.tvSubstatus, meta);
                views.setViewVisibility(R.id.tvSubstatus, meta.isEmpty() ? View.GONE : View.VISIBLE);
            } else if (gap != null) {
                applyBreakHero(views, false);
                views.setViewVisibility(R.id.tvKind, View.GONE);
                views.setTextViewText(R.id.tvStatus, localizedBreakLabel(context, false));
                String meta = AdvancedSettingsStore.showTimes(context) ? minuteLabel(gap.start) + " - " + minuteLabel(gap.end) : "";
                views.setTextViewText(R.id.tvSubstatus, meta);
                views.setViewVisibility(R.id.tvSubstatus, meta.isEmpty() ? View.GONE : View.VISIBLE);
            } else if (next != null) {
                applyHeroColor(views, next.course.slot, next.course.label);
                views.setTextViewText(R.id.tvKind, UiSettingsStore.t(context, "next"));
                views.setTextViewText(R.id.tvStatus, displayLabel(next.course));
                String meta = courseMeta(context, next.course);
                views.setTextViewText(R.id.tvSubstatus, meta);
                views.setViewVisibility(R.id.tvSubstatus, meta.isEmpty() ? View.GONE : View.VISIBLE);
                long minutesUntil = minutesUntil(now, next.date, next.course.start);
                if (minutesUntil >= 0 && minutesUntil < 24 * 60) {
                    views.setViewVisibility(R.id.tvRemaining, View.VISIBLE);
                    views.setTextViewText(R.id.tvRemaining, countdownLabel(context, minutesUntil));
                }
            } else {
                views.setInt(R.id.currentCard, "setBackgroundColor", 0xFFE9F0F7);
                views.setViewVisibility(R.id.tvKind, View.GONE);
                views.setTextViewText(R.id.tvStatus, UiSettingsStore.t(context, "noCourse"));
                views.setTextViewText(R.id.tvSubstatus, "");
                views.setViewVisibility(R.id.tvSubstatus, View.GONE);
            }
        }

        Intent listIntent = new Intent(context, UpcomingCoursesService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        listIntent.setData(Uri.parse("edt://widget/" + widgetId + "/" + (dayMode ? "day" : "summary")));
        views.setRemoteAdapter(R.id.upcomingList, listIntent);
        views.setEmptyView(R.id.upcomingList, R.id.emptyUpcoming);
        views.setTextViewText(R.id.emptyUpcoming, dayMode ? localizedNoCourseToday(context) : localizedNoOtherCourse(context));

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("open_mode", "week");
        PendingIntent openPending = PendingIntent.getActivity(context, 100 + widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);
        views.setOnClickPendingIntent(R.id.widgetHeader, openPending);
        views.setOnClickPendingIntent(R.id.currentCard, openPending);

        Intent toggleIntent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_TOGGLE_MODE);
        toggleIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent togglePending = PendingIntent.getBroadcast(context, 3000 + widgetId, toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnWidgetMode, togglePending);

        Intent listOpenIntent = new Intent(context, MainActivity.class);
        listOpenIntent.putExtra("open_mode", "week");
        PendingIntent listPending = PendingIntent.getActivity(context, 4000 + widgetId, listOpenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.upcomingList, listPending);

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
        int surface = theme.widgetSurface, card = theme.widgetCard, ink = theme.ink, muted = theme.muted;
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) {
            surface = 0xFFFFFFFF; card = 0xFFFFFFFF; ink = 0xFF000000; muted = 0xFF303030;
        }
        views.setInt(R.id.widgetRoot, "setBackgroundColor", surface);
        views.setInt(R.id.currentCard, "setBackgroundColor", card);
        views.setTextColor(R.id.tvStatus, ink);
        views.setTextColor(R.id.tvSubstatus, muted);
        views.setTextColor(R.id.tvTileDate, 0xFFFFFFFF);
        views.setTextColor(R.id.tvRemaining, 0xFF9A2342);
        views.setTextColor(R.id.emptyUpcoming, muted);
        views.setViewVisibility(R.id.classProgress, View.GONE);
        views.setViewVisibility(R.id.tvProgressPercent, View.GONE);

        String density = AdvancedSettingsStore.density(context);
        float densityScale = "compact".equals(density) ? .94f : ("comfortable".equals(density) ? 1.08f : 1f);
        views.setTextViewTextSize(R.id.tvKind, TypedValue.COMPLEX_UNIT_SP, 9f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvKindActive, TypedValue.COMPLEX_UNIT_SP, 9f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvStatus, TypedValue.COMPLEX_UNIT_SP, 18f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvSubstatus, TypedValue.COMPLEX_UNIT_SP, 11f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvTileDate, TypedValue.COMPLEX_UNIT_SP, 11f * scale * densityScale);
        views.setTextViewTextSize(R.id.tvRemaining, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.btnWidgetMode, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);
        views.setTextViewTextSize(R.id.emptyUpcoming, TypedValue.COMPLEX_UNIT_SP, 10f * scale * densityScale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setColorStateList(R.id.classProgress, "setProgressTintList", ColorStateList.valueOf(theme.accent));
            views.setColorStateList(R.id.classProgress, "setProgressBackgroundTintList", ColorStateList.valueOf(theme.progressTrack));
        }
    }

    private static void applyHeroColor(RemoteViews views, int slot, String label) {
        int color = courseColor(slot, label);
        boolean dark = darkText(slot);
        int ink = dark ? 0xFF17213A : 0xFFFFFFFF;
        int muted = dark ? 0xFF35435A : 0xFFF7FBFF;
        views.setInt(R.id.currentCard, "setBackgroundColor", color);
        views.setTextColor(R.id.tvStatus, ink);
        views.setTextColor(R.id.tvSubstatus, muted);
        views.setTextColor(R.id.tvKind, dark ? 0xFF72570B : 0xFF9A2342);
        views.setTextColor(R.id.tvKindActive, dark ? 0xFF72570B : 0xFF9A2342);
        views.setTextColor(R.id.tvRemaining, dark ? 0xFF72570B : 0xFF9A2342);
    }

    private static void applyBreakHero(RemoteViews views, boolean lunch) {
        if (lunch) {
            views.setInt(R.id.currentCard, "setBackgroundColor", 0xFFE9F0F7);
            views.setTextColor(R.id.tvStatus, 0xFF173653);
            views.setTextColor(R.id.tvSubstatus, 0xFF4E6578);
        } else {
            views.setInt(R.id.currentCard, "setBackgroundColor", 0xFFF0E9FF);
            views.setTextColor(R.id.tvStatus, 0xFF5E3E8E);
            views.setTextColor(R.id.tvSubstatus, 0xFF705A89);
        }
    }

    private static int courseColor(int slot, String label) {
        int[] palette = {0xFFF0335D,0xFFFF7B2F,0xFFFFEF88,0xFF21C877,0xFF18B9BE,0xFF2F83E8,0xFF9B55E9};
        int index = slot > 0 ? slot - 1 : Math.abs(String.valueOf(label).hashCode());
        return palette[Math.floorMod(index, palette.length)];
    }

    private static boolean darkText(int slot) {
        return Math.floorMod(Math.max(1, slot) - 1, 7) == 2;
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
        String label = new SimpleDateFormat("EEE d MMM", locale).format(date.getTime());
        if (!label.isEmpty()) label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
        if (!AdvancedSettingsStore.showWeekInfo(context)) return label;
        return label + " · S" + date.get(Calendar.WEEK_OF_YEAR) + " · " + ScheduleStore.getWeekLetter(context, date);
    }

    private static long minutesUntil(Calendar now, Calendar date, String start) {
        Calendar target = (Calendar) date.clone();
        int m = ScheduleData.toMinutes(start);
        target.set(Calendar.HOUR_OF_DAY, m / 60);
        target.set(Calendar.MINUTE, m % 60);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        return Math.max(0L, (target.getTimeInMillis() - now.getTimeInMillis()) / 60000L);
    }

    private static String countdownLabel(Context context, long minutes) {
        if (minutes < 60) return UiSettingsStore.t(context, "in") + " " + Math.max(1, minutes) + " min";
        long h = Math.max(1, Math.round(minutes / 60.0));
        return UiSettingsStore.t(context, "in") + " " + h + " h";
    }

    private static String shortMinutes(Context context, int minutes) {
        return Math.max(1, minutes) + " min";
    }

    private static String localizedDay(Context context) {
        String l = UiSettingsStore.language(context);
        return "de".equals(l) ? "Tag" : ("en".equals(l) ? "Day" : "Journée");
    }

    private static String localizedTwoCourses(Context context) {
        String l = UiSettingsStore.language(context);
        return "de".equals(l) ? "2 Std." : ("en".equals(l) ? "2 classes" : "2 cours");
    }

    private static String localizedNoCourseToday(Context context) {
        String l = UiSettingsStore.language(context);
        return "de".equals(l) ? "Heute kein Unterricht" : ("en".equals(l) ? "No classes today" : "Aucun cours aujourd’hui");
    }

    private static String localizedNoOtherCourse(Context context) {
        String l = UiSettingsStore.language(context);
        return "de".equals(l) ? "Keine weitere Stunde" : ("en".equals(l) ? "No other class scheduled" : "Aucun autre cours programmé");
    }

    private static String minuteLabel(int minute) { return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60); }
    private static String room(String room) { return room == null || room.trim().isEmpty() ? "—" : room; }

    private static void scheduleNextBoundary(Context context) {
        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis(), targetMs = Long.MAX_VALUE;
        int nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> todayCourses = ScheduleStore.getCourses(context, now);
        boolean needsMinuteTick = false;
        for (ScheduleData.Course c : todayCourses) {
            int start = ScheduleData.toMinutes(c.start), end = ScheduleData.toMinutes(c.end);
            if (nowMinute >= start && nowMinute < end) { needsMinuteTick = true; break; }
            if (start > nowMinute && start - nowMinute <= 180) needsMinuteTick = true;
        }
        if (needsMinuteTick) {
            Calendar tick = (Calendar) now.clone();
            tick.add(Calendar.MINUTE, 1); tick.set(Calendar.SECOND, 2); tick.set(Calendar.MILLISECOND, 0);
            targetMs = tick.getTimeInMillis();
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
