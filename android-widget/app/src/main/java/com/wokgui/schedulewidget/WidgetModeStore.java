package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

final class WidgetModeStore {
    private static final String PREFS = "widget_display_modes_v1";
    private static final String PREFIX = "day_mode_";
    private static final String HIDDEN_PREFIX = "mode_button_hidden_";

    private WidgetModeStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean isDayMode(Context context, int widgetId) {
        return prefs(context).getBoolean(PREFIX + widgetId, false);
    }

    static boolean isModeButtonHidden(Context context, int widgetId) {
        return prefs(context).getBoolean(HIDDEN_PREFIX + widgetId, false);
    }

    static boolean toggle(Context context, int widgetId) {
        boolean next = !isDayMode(context, widgetId);
        prefs(context).edit().putBoolean(PREFIX + widgetId, next).apply();
        return next;
    }

    static boolean toggleAndHide(Context context, int widgetId) {
        boolean next = !isDayMode(context, widgetId);
        prefs(context).edit()
                .putBoolean(PREFIX + widgetId, next)
                .putBoolean(HIDDEN_PREFIX + widgetId, true)
                .apply();
        return next;
    }

    static void clear(Context context, int widgetId) {
        prefs(context).edit()
                .remove(PREFIX + widgetId)
                .remove(HIDDEN_PREFIX + widgetId)
                .apply();
    }
}
