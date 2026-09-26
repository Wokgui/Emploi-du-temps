package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;

/** Stores the visual format selected independently for each home-screen widget instance. */
final class WidgetLayoutStore {
    static final int FORMAT_CLASSIC = 1;
    static final int FORMAT_CONDENSED = 3;
    static final int FORMAT_MINI = 4;

    private static final String PREFS = "widget_layout_formats_v1";
    private static final String PREFIX = "format_";

    private WidgetLayoutStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean has(Context context, int widgetId) {
        return prefs(context).contains(PREFIX + widgetId);
    }

    static int get(Context context, int widgetId) {
        int value = prefs(context).getInt(PREFIX + widgetId, FORMAT_CLASSIC);
        return isSupported(value) ? value : FORMAT_CLASSIC;
    }

    static int enforceProviderFormat(Context context, AppWidgetManager manager, int widgetId) {
        int expected = formatForWidget(context, manager, widgetId);
        if (!has(context, widgetId) || get(context, widgetId) != expected) set(context, widgetId, expected);
        return expected;
    }

    static int formatForProviderClass(Class<?> providerClass) {
        if (providerClass == ScheduleWidgetCondensedProvider.class) return FORMAT_CONDENSED;
        if (providerClass == ScheduleWidgetMiniProvider.class) return FORMAT_MINI;
        return FORMAT_CLASSIC;
    }

    private static int formatForWidget(Context context, AppWidgetManager manager, int widgetId) {
        try {
            AppWidgetProviderInfo info = manager == null ? null : manager.getAppWidgetInfo(widgetId);
            String className = info == null || info.provider == null ? "" : info.provider.getClassName();
            if (ScheduleWidgetCondensedProvider.class.getName().equals(className)) return FORMAT_CONDENSED;
            if (ScheduleWidgetMiniProvider.class.getName().equals(className)) return FORMAT_MINI;
            if (ScheduleWidgetProvider.class.getName().equals(className)) return FORMAT_CLASSIC;
        } catch (Exception ignored) {}
        return get(context, widgetId);
    }

    static void set(Context context, int widgetId, int format) {
        int safe = isSupported(format) ? format : FORMAT_CLASSIC;
        prefs(context).edit().putInt(PREFIX + widgetId, safe).apply();
    }

    static void clear(Context context, int widgetId) {
        prefs(context).edit().remove(PREFIX + widgetId).apply();
    }

    private static boolean isSupported(int value) {
        return value == FORMAT_CLASSIC || value == FORMAT_CONDENSED || value == FORMAT_MINI;
    }
}
