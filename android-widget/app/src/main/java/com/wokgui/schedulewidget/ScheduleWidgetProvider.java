package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.List;

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
        String action = intent == null ? null : intent.getAction();
        if (ACTION_REFRESH.equals(action)
                || ACTION_BOUNDARY.equals(action)
                || ACTION_TOGGLE_MODE.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)) {
            updateAll(context);
            scheduleNextBoundary(context);
            ReminderScheduler.reschedule(context);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager, int appWidgetId, android.os.Bundle newOptions) {
        manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.upcomingList);
        updateWidget(context, manager, appWidgetId);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int id : appWidgetIds) WidgetModeStore.clear(context, id);
    }

    @Override
    public void onEnabled(Context context) {
        ScheduleStore.ensureInitialized(context);
        updateAll(context);
        scheduleNextBoundary(context);
        ReminderScheduler.reschedule(context);
    }

    @Override
    public void onDisabled(Context context) {
        cancelBoundary(context);
    }

    static void refreshAll(Context context) {
        updateAll(context);
    }

    private static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScheduleWidgetProvider.class));
        if (ids == null || ids.length == 0) return;
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        ScheduleStore.ensureInitialized(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);

        // Le nouveau widget n'a plus de bandeau ni de tuile « prochain cours » séparée.
        views.setViewVisibility(R.id.widgetHeader, View.GONE);
        views.setViewVisibility(R.id.currentCard, View.GONE);
        views.setViewVisibility(R.id.btnWidgetMode, View.GONE);

        Intent listIntent = new Intent(context, UpcomingCoursesService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        listIntent.setData(Uri.parse("edt://widget/" + widgetId + "/courses"));
        views.setRemoteAdapter(R.id.upcomingList, listIntent);
        views.setEmptyView(R.id.upcomingList, R.id.emptyUpcoming);
        views.setTextViewText(R.id.emptyUpcoming, UiSettingsStore.t(context, "noCourse"));

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("open_mode", "week");
        PendingIntent openPending = PendingIntent.getActivity(
                context,
                100 + widgetId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);
        views.setOnClickPendingIntent(R.id.emptyUpcoming, openPending);

        Intent rowIntent = new Intent(context, MainActivity.class);
        rowIntent.putExtra("open_mode", "week");
        PendingIntent rowPending = PendingIntent.getActivity(
                context,
                4000 + widgetId,
                rowIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setPendingIntentTemplate(R.id.upcomingList, rowPending);

        manager.updateAppWidget(widgetId, views);
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.upcomingList);
    }

    private static void scheduleNextBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;

        long nowMs = System.currentTimeMillis();
        long next = nowMs + 15L * 60L * 1000L;
        Calendar now = Calendar.getInstance();

        Calendar midnight = (Calendar) now.clone();
        midnight.add(Calendar.DAY_OF_YEAR, 1);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 2);
        midnight.set(Calendar.MILLISECOND, 0);
        next = Math.min(next, midnight.getTimeInMillis());

        Calendar cursor = (Calendar) now.clone();
        for (int day = 0; day < 2; day++) {
            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, cursor);
            if (courses != null) {
                for (ScheduleData.Course c : courses) {
                    long start = boundaryMillis(cursor, c.start);
                    long end = boundaryMillis(cursor, c.end);
                    if (start > nowMs + 1000L) next = Math.min(next, start + 1000L);
                    if (end > nowMs + 1000L) next = Math.min(next, end + 1000L);
                }
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }

        PendingIntent pending = boundaryPendingIntent(context);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarm.canScheduleExactAlarms()) {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending);
            } else {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pending);
            }
        } catch (Exception ignored) {
            try { alarm.set(AlarmManager.RTC_WAKEUP, next, pending); }
            catch (Exception ignoredAgain) {}
        }
    }

    private static long boundaryMillis(Calendar date, String hhmm) {
        int minute = ScheduleData.toMinutes(hhmm);
        Calendar c = (Calendar) date.clone();
        c.set(Calendar.HOUR_OF_DAY, minute / 60);
        c.set(Calendar.MINUTE, minute % 60);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static PendingIntent boundaryPendingIntent(Context context) {
        Intent i = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        return PendingIntent.getBroadcast(
                context,
                9107,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) alarm.cancel(boundaryPendingIntent(context));
    }
}
