package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

final class ReminderScheduler {
    static final String EXTRA_LABEL = "label";
    static final String EXTRA_ROOM = "room";
    static final String EXTRA_START = "start";
    static final String EXTRA_MINUTES = "minutes";
    private static final int REQUEST = 7101;

    private ReminderScheduler() {}

    static void reschedule(Context context) {
        cancel(context);
        if (!AdvancedSettingsStore.remindersEnabled(context)) return;

        Calendar now = Calendar.getInstance();
        long nowMs = now.getTimeInMillis();
        int lead = AdvancedSettingsStore.reminderMinutes(context);
        Calendar cursor = (Calendar) now.clone();

        for (int add = 0; add < 21; add++) {
            List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, cursor);
            for (ScheduleData.Course c : courses) {
                Calendar start = (Calendar) cursor.clone();
                int m = ScheduleData.toMinutes(c.start);
                start.set(Calendar.HOUR_OF_DAY, m / 60);
                start.set(Calendar.MINUTE, m % 60);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                if (start.getTimeInMillis() <= nowMs) continue;

                long trigger = start.getTimeInMillis() - lead * 60_000L;
                if (trigger <= nowMs) trigger = nowMs + 2_000L;
                schedule(context, trigger, c, lead);
                return;
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
            cursor.set(Calendar.HOUR_OF_DAY, 0);
            cursor.set(Calendar.MINUTE, 0);
            cursor.set(Calendar.SECOND, 0);
            cursor.set(Calendar.MILLISECOND, 0);
        }
    }

    private static void schedule(Context context, long triggerAt, ScheduleData.Course course, int lead) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        Intent intent = new Intent(context, ReminderReceiver.class)
                .putExtra(EXTRA_LABEL, course.label)
                .putExtra(EXTRA_ROOM, course.room)
                .putExtra(EXTRA_START, course.start)
                .putExtra(EXTRA_MINUTES, lead);
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
    }

    static void cancel(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        PendingIntent pending = PendingIntent.getBroadcast(context, REQUEST,
                new Intent(context, ReminderReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pending);
    }
}
