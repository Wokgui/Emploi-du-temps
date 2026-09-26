package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/** Launcher-visible compact 2x2-style widget preset using the mini renderer. */
public class ScheduleWidgetMiniProvider extends ScheduleWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        if (appWidgetIds != null) {
            for (int id : appWidgetIds) {
                WidgetLayoutStore.set(context, id, WidgetLayoutStore.FORMAT_MINI);
            }
        }
        super.onUpdate(context, manager, appWidgetIds);
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
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, ScheduleWidgetMiniProvider.class));
            if (ids != null && ids.length > 0) onUpdate(context, manager, ids);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if (appWidgetIds != null) for (int id : appWidgetIds) WidgetLayoutStore.clear(context, id);
    }
}
