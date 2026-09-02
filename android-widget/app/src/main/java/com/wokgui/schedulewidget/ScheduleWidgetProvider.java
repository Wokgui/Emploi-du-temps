package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

public class ScheduleWidgetProvider extends AppWidgetProvider {
    static final String ACTION_REFRESH = "com.wokgui.schedulewidget.REFRESH";
    static final String ACTION_BOUNDARY = "com.wokgui.schedulewidget.BOUNDARY";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) updateWidget(context, manager, id);
        scheduleNextBoundary(context);
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
        }
    }

    @Override
    public void onEnabled(Context context) {
        updateAll(context);
        scheduleNextBoundary(context);
    }

    @Override
    public void onDisabled(Context context) {
        cancelBoundary(context);
    }

    private static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScheduleWidgetProvider.class));
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DAY_OF_WEEK);
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> courses = ScheduleData.forDay(day);

        ScheduleData.Course current = null;
        ScheduleData.Course next = null;
        int completed = 0;
        double courseProgressUnits = 0.0;

        for (ScheduleData.Course course : courses) {
            int start = ScheduleData.toMinutes(course.start);
            int end = ScheduleData.toMinutes(course.end);
            if (minute >= end) {
                completed++;
                courseProgressUnits += 1.0;
            } else if (minute >= start) {
                current = course;
                courseProgressUnits += Math.max(0.0, Math.min(1.0, (minute - start) / (double) (end - start)));
            } else if (next == null) {
                next = course;
            }
        }

        int dayProgress = clamp((int) Math.round((minute - 480) * 100.0 / 600.0));
        int classProgress = courses.isEmpty() ? 0 : clamp((int) Math.round(courseProgressUnits * 100.0 / courses.size()));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        views.setTextViewText(R.id.tvTitle, dayLabel(day) + (courses.isEmpty() ? "" : " · " + completed + "/" + courses.size() + " cours"));
        views.setProgressBar(R.id.dayProgress, 100, dayProgress, false);
        views.setProgressBar(R.id.classProgress, 100, classProgress, false);
        views.setTextViewText(R.id.tvProgress, "Journée " + dayProgress + "% · Cours " + classProgress + "%");

        if (current != null) {
            int end = ScheduleData.toMinutes(current.end);
            long remainingMs = Math.max(0L, (end - minute) * 60_000L - now.get(Calendar.SECOND) * 1000L);
            views.setTextViewText(R.id.tvStatus, current.label + " · salle " + current.room);
            views.setTextViewText(R.id.tvSubstatus, "Cours en cours · fin à " + current.end);
            showCountdown(views, remainingMs, "Reste %s");
        } else if (next != null) {
            int start = ScheduleData.toMinutes(next.start);
            long remainingMs = Math.max(0L, (start - minute) * 60_000L - now.get(Calendar.SECOND) * 1000L);
            views.setTextViewText(R.id.tvStatus, "Prochain : " + next.label + " · salle " + next.room);
            views.setTextViewText(R.id.tvSubstatus, "À " + next.start);
            showCountdown(views, remainingMs, "Dans %s");
        } else if (courses.isEmpty()) {
            views.setTextViewText(R.id.tvStatus, "Pas de cours aujourd’hui");
            views.setTextViewText(R.id.tvSubstatus, nextCourseText(now));
            views.setViewVisibility(R.id.countdown, View.GONE);
        } else {
            views.setTextViewText(R.id.tvStatus, "Cours terminés pour aujourd’hui");
            views.setTextViewText(R.id.tvSubstatus, nextCourseText(now));
            views.setViewVisibility(R.id.countdown, View.GONE);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);

        Intent refreshIntent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(context, 1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPending);

        manager.updateAppWidget(widgetId, views);
    }

    private static void showCountdown(RemoteViews views, long remainingMs, String format) {
        views.setViewVisibility(R.id.countdown, View.VISIBLE);
        long base = SystemClock.elapsedRealtime() + remainingMs;
        views.setChronometer(R.id.countdown, base, format, true);
        views.setChronometerCountDown(R.id.countdown, true);
    }

    private static String nextCourseText(Calendar from) {
        Calendar cursor = (Calendar) from.clone();
        for (int add = 1; add <= 7; add++) {
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            List<ScheduleData.Course> list = ScheduleData.forDay(cursor.get(Calendar.DAY_OF_WEEK));
            if (!list.isEmpty()) {
                ScheduleData.Course c = list.get(0);
                return "Prochain : " + dayLabel(cursor.get(Calendar.DAY_OF_WEEK)) + " " + c.start + " · " + c.label;
            }
        }
        return "";
    }

    private static String dayLabel(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Lundi";
            case Calendar.TUESDAY: return "Mardi";
            case Calendar.WEDNESDAY: return "Mercredi";
            case Calendar.THURSDAY: return "Jeudi";
            case Calendar.FRIDAY: return "Vendredi";
            case Calendar.SATURDAY: return "Samedi";
            default: return "Dimanche";
        }
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static void scheduleNextBoundary(Context context) {
        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis();
        long targetMs = Long.MAX_VALUE;

        Calendar day = (Calendar) now.clone();
        for (int add = 0; add <= 7; add++) {
            int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
            for (int boundaryMin : ScheduleData.allBoundariesForDay(dayOfWeek)) {
                Calendar candidate = (Calendar) day.clone();
                candidate.set(Calendar.HOUR_OF_DAY, boundaryMin / 60);
                candidate.set(Calendar.MINUTE, boundaryMin % 60);
                candidate.set(Calendar.SECOND, 2);
                candidate.set(Calendar.MILLISECOND, 0);
                long candidateMs = candidate.getTimeInMillis();
                if (candidateMs > nowMs + 1000 && candidateMs < targetMs) targetMs = candidateMs;
            }
            day.add(Calendar.DAY_OF_YEAR, 1);
            day.set(Calendar.HOUR_OF_DAY, 0);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
        }

        if (targetMs == Long.MAX_VALUE) return;
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMs, pending);
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pending);
    }
}
