package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

public class ScheduleWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.wokgui.schedulewidget.REFRESH";
    static final String ACTION_BOUNDARY = "com.wokgui.schedulewidget.BOUNDARY";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        ScheduleStore.ensureInitialized(context);
        for (int id : ids) updateWidget(context, manager, id);
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        scheduleNextBoundary(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action) || ACTION_BOUNDARY.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)) {
            updateAll(context);
            scheduleNextBoundary(context);
        }
    }

    @Override public void onEnabled(Context context) { updateAll(context); scheduleNextBoundary(context); }
    @Override public void onDisabled(Context context) { cancelBoundary(context); }

    private static void updateAll(Context context) {
        ScheduleStore.ensureInitialized(context);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScheduleWidgetProvider.class));
        for (int id : ids) updateWidget(context, manager, id);
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, day);
        ScheduleData.Course current = null;
        for (ScheduleData.Course course : courses) {
            int start = ScheduleData.toMinutes(course.start), end = ScheduleData.toMinutes(course.end);
            if (minute >= start && minute < end) { current = course; break; }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        if (current != null) {
            int start = ScheduleData.toMinutes(current.start), end = ScheduleData.toMinutes(current.end);
            int progress = clamp((int)Math.round((minute - start) * 100.0 / Math.max(1, end - start)));
            long remainingMs = Math.max(0L, (end - minute) * 60_000L - now.get(Calendar.SECOND) * 1000L);
            views.setTextViewText(R.id.tvKind, "Cours en cours");
            views.setTextViewText(R.id.tvStatus, current.label);
            views.setTextViewText(R.id.tvSubstatus, "En cours · jusqu’à " + current.end + " · salle " + room(current.room));
            showCountdown(views, remainingMs, "Encore %s");
            views.setProgressBar(R.id.classProgress, 100, progress, false);
            views.setViewVisibility(R.id.classProgress, View.VISIBLE);
        } else {
            NextCourseInfo next = findNextCourse(context, now);
            views.setTextViewText(R.id.tvKind, "Prochain cours");
            if (next != null) {
                views.setTextViewText(R.id.tvStatus, next.course.label);
                String prefix = isSameCalendarDay(now, next.date) ? "" : dayLabel(next.date.get(Calendar.DAY_OF_WEEK)) + " ";
                views.setTextViewText(R.id.tvSubstatus, prefix + next.course.start + " · salle " + room(next.course.room));
                long remainingMs = Math.max(0L, next.date.getTimeInMillis() - now.getTimeInMillis());
                showCountdown(views, remainingMs, "Dans %s");
            } else {
                views.setTextViewText(R.id.tvStatus, "Aucun cours programmé");
                views.setTextViewText(R.id.tvSubstatus, "Touchez ✎ pour modifier l’emploi du temps");
                views.setViewVisibility(R.id.countdown, View.GONE);
            }
            views.setViewVisibility(R.id.classProgress, View.GONE);
        }

        Intent serviceIntent = new Intent(context, UpcomingCoursesService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.upcomingList, serviceIntent);
        views.setEmptyView(R.id.upcomingList, R.id.emptyUpcoming);

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(context, widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);

        Intent editIntent = new Intent(context, MainActivity.class).putExtra("open_mode", "edit");
        PendingIntent editPending = PendingIntent.getActivity(context, widgetId + 1000, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnEdit, editPending);
        views.setPendingIntentTemplate(R.id.upcomingList, editPending);

        Intent refreshIntent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(context, widgetId + 2000, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPending);
        manager.updateAppWidget(widgetId, views);
    }

    private static String room(String room) { return room == null || room.isEmpty() ? "—" : room; }

    private static void showCountdown(RemoteViews views, long remainingMs, String format) {
        views.setViewVisibility(R.id.countdown, View.VISIBLE);
        long base = SystemClock.elapsedRealtime() + remainingMs;
        views.setChronometer(R.id.countdown, base, format, true);
        views.setChronometerCountDown(R.id.countdown, true);
    }

    private static NextCourseInfo findNextCourse(Context context, Calendar now) {
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        Calendar cursor = (Calendar) now.clone();
        for (int add = 0; add <= 7; add++) {
            int day = cursor.get(Calendar.DAY_OF_WEEK);
            for (ScheduleData.Course c : ScheduleStore.getCourses(context, day)) {
                if (add == 0 && ScheduleData.toMinutes(c.start) <= nowMin) continue;
                Calendar date = (Calendar) cursor.clone();
                int start = ScheduleData.toMinutes(c.start);
                date.set(Calendar.HOUR_OF_DAY, start / 60);
                date.set(Calendar.MINUTE, start % 60);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                return new NextCourseInfo(date, c);
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            cursor.set(Calendar.HOUR_OF_DAY, 0); cursor.set(Calendar.MINUTE, 0); cursor.set(Calendar.SECOND, 0); cursor.set(Calendar.MILLISECOND, 0);
        }
        return null;
    }

    private static boolean isSameCalendarDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private static String dayLabel(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Lundi";
            case Calendar.TUESDAY: return "Mardi";
            case Calendar.WEDNESDAY: return "Mercredi";
            case Calendar.THURSDAY: return "Jeudi";
            case Calendar.FRIDAY: return "Vendredi";
            default: return "";
        }
    }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }

    private static void scheduleNextBoundary(Context context) {
        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis(), targetMs = Long.MAX_VALUE;
        Calendar day = (Calendar) now.clone();
        for (int add = 0; add <= 7; add++) {
            for (int boundaryMin : ScheduleData.boundaries(ScheduleStore.getCourses(context, day.get(Calendar.DAY_OF_WEEK)))) {
                Calendar candidate = (Calendar) day.clone();
                candidate.set(Calendar.HOUR_OF_DAY, boundaryMin / 60);
                candidate.set(Calendar.MINUTE, boundaryMin % 60);
                candidate.set(Calendar.SECOND, 2);
                candidate.set(Calendar.MILLISECOND, 0);
                long t = candidate.getTimeInMillis();
                if (t > nowMs + 1000 && t < targetMs) targetMs = t;
            }
            day.add(Calendar.DAY_OF_YEAR, 1);
            day.set(Calendar.HOUR_OF_DAY, 0); day.set(Calendar.MINUTE, 0); day.set(Calendar.SECOND, 0); day.set(Calendar.MILLISECOND, 0);
        }
        if (targetMs == Long.MAX_VALUE) return;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2,
                new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMs, pending);
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2,
                new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pending);
    }

    private static class NextCourseInfo {
        final Calendar date; final ScheduleData.Course course;
        NextCourseInfo(Calendar date, ScheduleData.Course course) { this.date = date; this.course = course; }
    }
}
