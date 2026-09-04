package com.wokgui.schedulewidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

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
        ScheduleStore.ensureInitialized(context);
        updateAll(context);
        scheduleNextBoundary(context);
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
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        for (int id : ids) updateWidget(context, manager, id);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        ScheduleStore.ensureInitialized(context);

        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_WEEK);
        int minute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> courses = ScheduleStore.getCourses(context, today);

        ScheduleData.Course current = null;
        for (ScheduleData.Course c : courses) {
            int s = ScheduleData.toMinutes(c.start);
            int e = ScheduleData.toMinutes(c.end);
            if (minute >= s && minute < e) {
                current = c;
                break;
            }
        }

        int lunchStart = ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4));
        int lunchEnd = ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5));
        boolean lunchValid = lunchEnd > lunchStart;
        boolean inLunch = current == null && !courses.isEmpty() && lunchValid
                && minute >= lunchStart && minute < lunchEnd;
        GapInfo gap = current == null && !inLunch
                ? findCurrentGap(courses, minute, lunchStart, lunchEnd)
                : null;

        NextCourseInfo next = findNextCourse(context, now);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_schedule);
        views.setTextColor(R.id.tvKind, 0xFF0AA6A6);
        views.setTextColor(R.id.tvStatus, 0xFF101936);
        views.setTextColor(R.id.tvSubstatus, 0xFF5A667A);

        if (current != null) {
            views.setTextViewText(R.id.tvKind, "Cours en cours");
            views.setTextViewText(R.id.tvStatus, current.label);
            views.setTextViewText(R.id.tvSubstatus,
                    current.start + "–" + current.end + " · salle " + room(current.room));
        } else if (inLunch) {
            String start = ScheduleStore.getSlotEnd(context, 4);
            String end = ScheduleStore.getSlotStart(context, 5);
            views.setTextViewText(R.id.tvKind, "Pause de midi");
            views.setTextViewText(R.id.tvStatus, start + "–" + end);
            views.setTextViewText(R.id.tvSubstatus, "Reprise à " + end);
            views.setTextColor(R.id.tvKind, 0xFF9A5C09);
            views.setTextColor(R.id.tvStatus, 0xFF7D5826);
            views.setTextColor(R.id.tvSubstatus, 0xFF8B6A3A);
        } else if (gap != null) {
            views.setTextViewText(R.id.tvKind, "Trou dans l’emploi du temps");
            views.setTextViewText(R.id.tvStatus,
                    minuteLabel(gap.start) + "–" + minuteLabel(gap.end)
                            + " · " + durationLabel(gap.end - gap.start));
            views.setTextViewText(R.id.tvSubstatus,
                    "Prochain cours à " + gap.next.start + " · " + gap.next.label);
            views.setTextColor(R.id.tvKind, 0xFF7357B8);
            views.setTextColor(R.id.tvStatus, 0xFF4F3A88);
            views.setTextColor(R.id.tvSubstatus, 0xFF6F6287);
        } else if (next != null) {
            views.setTextViewText(R.id.tvKind, "Prochain cours");
            views.setTextViewText(R.id.tvStatus, next.course.label);
            String prefix = isSameDay(now, next.date) ? "" : dayLabel(next.date.get(Calendar.DAY_OF_WEEK)) + " ";
            views.setTextViewText(R.id.tvSubstatus,
                    prefix + next.course.start + " · salle " + room(next.course.room));
        } else {
            views.setTextViewText(R.id.tvKind, "Emploi du temps");
            views.setTextViewText(R.id.tvStatus, "Aucun cours programmé");
            views.setTextViewText(R.id.tvSubstatus, "");
        }

        if (current != null) {
            int progress = courseProgress(current, minute);
            views.setViewVisibility(R.id.classProgress, View.VISIBLE);
            views.setProgressBar(R.id.classProgress, 100, progress, false);
        } else {
            views.setProgressBar(R.id.classProgress, 100, 0, false);
            views.setViewVisibility(R.id.classProgress, View.GONE);
        }

        Intent listIntent = new Intent(context, UpcomingCoursesService.class);
        listIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        listIntent.setData(Uri.parse(listIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.upcomingList, listIntent);
        views.setEmptyView(R.id.upcomingList, R.id.emptyUpcoming);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("open_mode", "today");
        PendingIntent openPending = PendingIntent.getActivity(
                context, 100 + widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widgetRoot, openPending);

        Intent editIntent = new Intent(context, MainActivity.class);
        editIntent.putExtra("open_mode", "edit");
        PendingIntent editPending = PendingIntent.getActivity(
                context, 200 + widgetId, editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnEdit, editPending);

        Intent refreshIntent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(
                context, 300 + widgetId, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.btnRefresh, refreshPending);

        views.setPendingIntentTemplate(R.id.upcomingList, editPending);
        manager.updateAppWidget(widgetId, views);
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.upcomingList);
    }

    private static GapInfo findCurrentGap(List<ScheduleData.Course> courses, int minute,
                                          int lunchStart, int lunchEnd) {
        if (courses == null || courses.size() < 2) return null;

        ScheduleData.Course previous = null;
        ScheduleData.Course next = null;
        int previousEnd = -1;
        int nextStart = Integer.MAX_VALUE;

        for (ScheduleData.Course c : courses) {
            int start = ScheduleData.toMinutes(c.start);
            int end = ScheduleData.toMinutes(c.end);
            if (end <= minute && end > previousEnd) {
                previous = c;
                previousEnd = end;
            }
            if (start > minute && start < nextStart) {
                next = c;
                nextStart = start;
            }
        }

        if (previous == null || next == null || nextStart <= previousEnd
                || minute < previousEnd || minute >= nextStart) return null;

        boolean lunchValid = lunchEnd > lunchStart;
        if (lunchValid && minute >= lunchStart && minute < lunchEnd) return null;

        int start = previousEnd;
        int end = nextStart;
        if (lunchValid) {
            if (minute < lunchStart && end > lunchStart) {
                end = lunchStart;
            } else if (minute >= lunchEnd && start < lunchEnd) {
                start = lunchEnd;
            }
        }

        if (end <= start || minute < start || minute >= end) return null;
        return new GapInfo(start, end, next);
    }

    private static int courseProgress(ScheduleData.Course course, int minute) {
        if (course == null) return 0;
        int start = ScheduleData.toMinutes(course.start);
        int end = ScheduleData.toMinutes(course.end);
        int duration = end - start;
        if (duration <= 0 || minute <= start) return 0;
        if (minute >= end) return 100;
        return clamp((int) Math.round((minute - start) * 100.0 / duration));
    }

    private static NextCourseInfo findNextCourse(Context context, Calendar now) {
        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        Calendar cursor = (Calendar) now.clone();

        for (int add = 0; add < 8; add++) {
            int day = cursor.get(Calendar.DAY_OF_WEEK);
            List<ScheduleData.Course> list = ScheduleStore.getCourses(context, day);
            for (ScheduleData.Course c : list) {
                if (add == 0 && ScheduleData.toMinutes(c.start) <= nowMin) continue;
                return new NextCourseInfo((Calendar) cursor.clone(), c);
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }
        return null;
    }

    private static String minuteLabel(int minute) {
        return String.format(Locale.FRANCE, "%02d:%02d", minute / 60, minute % 60);
    }

    private static String durationLabel(int minutes) {
        if (minutes <= 0) return "";
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0 && m > 0) return h + " h " + m;
        if (h > 0) return h + " h";
        return m + " min";
    }

    private static boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private static String room(String room) {
        return room == null || room.trim().isEmpty() ? "—" : room;
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

        int nowMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        List<ScheduleData.Course> todayCourses = ScheduleStore.getCourses(
                context, now.get(Calendar.DAY_OF_WEEK));
        for (ScheduleData.Course c : todayCourses) {
            int start = ScheduleData.toMinutes(c.start);
            int end = ScheduleData.toMinutes(c.end);
            if (nowMinute >= start && nowMinute < end) {
                Calendar tick = (Calendar) now.clone();
                tick.add(Calendar.MINUTE, 1);
                tick.set(Calendar.SECOND, 2);
                tick.set(Calendar.MILLISECOND, 0);
                targetMs = tick.getTimeInMillis();
                break;
            }
        }

        Calendar day = (Calendar) now.clone();
        for (int add = 0; add <= 7; add++) {
            List<Integer> boundaries = ScheduleData.boundaries(
                    ScheduleStore.getCourses(context, day.get(Calendar.DAY_OF_WEEK)));
            boundaries.add(ScheduleData.toMinutes(ScheduleStore.getSlotEnd(context, 4)));
            boundaries.add(ScheduleData.toMinutes(ScheduleStore.getSlotStart(context, 5)));

            for (int boundaryMin : boundaries) {
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
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetMs, pending);
    }

    private static void cancelBoundary(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduleWidgetProvider.class).setAction(ACTION_BOUNDARY);
        PendingIntent pending = PendingIntent.getBroadcast(
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarm.cancel(pending);
    }

    private static class GapInfo {
        final int start;
        final int end;
        final ScheduleData.Course next;

        GapInfo(int start, int end, ScheduleData.Course next) {
            this.start = start;
            this.end = end;
            this.next = next;
        }
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
