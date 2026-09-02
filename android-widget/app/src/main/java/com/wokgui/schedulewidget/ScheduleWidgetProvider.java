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
        ScheduleData.Course nextToday = null;

        for (ScheduleData.Course course : courses) {
            int start = ScheduleData.toMinutes(course.start);
            int end = ScheduleData.toMinutes(course.end);
            if (minute >= start && minute < end) {
                current = course;
            } else if (start > minute && nextToday == null) {
                nextToday = course;
            }
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);

        if (current != null) {
            int start = ScheduleData.toMinutes(current.start);
            int end = ScheduleData.toMinutes(current.end);
            int progress = clamp((int) Math.round((minute - start) * 100.0 / Math.max(1, end - start)));
            long remainingMs = Math.max(0L, (end - minute) * 60_000L - now.get(Calendar.SECOND) * 1000L);

            views.setTextViewText(R.id.tvStatus, current.label);
            views.setTextViewText(R.id.tvSubstatus, "Salle " + current.room + " · jusqu’à " + current.end);
            showCountdown(views, remainingMs, "Encore %s");
            views.setProgressBar(R.id.classProgress, 100, progress, false);
            views.setViewVisibility(R.id.classProgress, View.VISIBLE);
        } else if (nextToday != null) {
            int start = ScheduleData.toMinutes(nextToday.start);
            long remainingMs = Math.max(0L, (start - minute) * 60_000L - now.get(Calendar.SECOND) * 1000L);

            views.setTextViewText(R.id.tvStatus, "Pas de cours maintenant");
            views.setTextViewText(R.id.tvSubstatus, "Prochain à " + nextToday.start + " · salle " + nextToday.room);
            showCountdown(views, remainingMs, "Dans %s");
            views.setViewVisibility(R.id.classProgress, View.GONE);
        } else if (courses.isEmpty()) {
            views.setTextViewText(R.id.tvStatus, "Pas de cours aujourd’hui");
            views.setTextViewText(R.id.tvSubstatus, "Prochain cours ci-dessous");
            views.setViewVisibility(R.id.countdown, View.GONE);
            views.setViewVisibility(R.id.classProgress, View.GONE);
        } else {
            views.setTextViewText(R.id.tvStatus, "Cours terminés");
            views.setTextViewText(R.id.tvSubstatus, "Pour aujourd’hui");
            views.setViewVisibility(R.id.countdown, View.GONE);
            views.setViewVisibility(R.id.classProgress, View.GONE);
        }

        NextCourseInfo nextInfo = findNextCourse(now, nextToday);
        if (nextInfo != null) {
            views.setViewVisibility(R.id.nextPanel, View.VISIBLE);
            views.setTextViewText(R.id.tvNextClass, nextInfo.course.label);
            String prefix = isSameCalendarDay(now, nextInfo.date) ? "" : dayLabel(nextInfo.date.get(Calendar.DAY_OF_WEEK)) + " ";
            views.setTextViewText(R.id.tvNextMeta, prefix + nextInfo.course.start + " · salle " + nextInfo.course.room);
        } else {
            views.setViewVisibility(R.id.nextPanel, View.GONE);
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPending = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);

        Intent refreshIntent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPending);

        manager.updateAppWidget(widgetId, views);
    }

    private static void showCountdown(RemoteViews views, long remainingMs, String format) {
        views.setViewVisibility(R.id.countdown, View.VISIBLE);
        long base = SystemClock.elapsedRealtime() + remainingMs;
        views.setChronometer(R.id.countdown, base, format, true);
        views.setChronometerCountDown(R.id.countdown, true);
    }

    private static NextCourseInfo findNextCourse(Calendar now, ScheduleData.Course nextToday) {
        if (nextToday != null) return new NextCourseInfo((Calendar) now.clone(), nextToday);

        Calendar cursor = (Calendar) now.clone();
        for (int add = 1; add <= 7; add++) {
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            List<ScheduleData.Course> list = ScheduleData.forDay(cursor.get(Calendar.DAY_OF_WEEK));
            if (!list.isEmpty()) {
                return new NextCourseInfo((Calendar) cursor.clone(), list.get(0));
            }
        }
        return null;
    }

    private static boolean isSameCalendarDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
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
        PendingIntent pending = PendingIntent.getBroadcast(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMs, pending);
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(
                context,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarm.cancel(pending);
    }

    private static class NextCourseInfo {
        final Calendar date;
        final ScheduleData.Course course;

        NextCourseInfo(Calendar date, ScheduleData.Course course) {
            this.date = date;
            this.course = course;
        }
    }
}
