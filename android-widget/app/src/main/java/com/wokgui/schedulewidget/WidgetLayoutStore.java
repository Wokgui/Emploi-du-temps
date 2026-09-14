package com.wokgui.schedulewidget;

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

    static int get(Context context, int widgetId) {
        int value = prefs(context).getInt(PREFIX + widgetId, FORMAT_CLASSIC);
        return isSupported(value) ? value : FORMAT_CLASSIC;
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
