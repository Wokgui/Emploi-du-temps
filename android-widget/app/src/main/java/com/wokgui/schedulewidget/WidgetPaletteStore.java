package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

final class WidgetPaletteStore {
    private static final String PREFS = "widget_palette_v1";
    private static final String KEY = "palette";
    private static final String DEFAULT = "vivid";

    private WidgetPaletteStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static String getPalette(Context context) {
        String id = prefs(context).getString(KEY, DEFAULT);
        return isValid(id) ? id : DEFAULT;
    }

    static void setPalette(Context context, String id) {
        prefs(context).edit().putString(KEY, isValid(id) ? id : DEFAULT).apply();
    }

    static int headerColor(Context context) {
        return 0xFF55616D;
    }

    static int courseColor(Context context, int slot, String label) {
        int[] palette = palette(context);
        int index = slot > 0 ? slot - 1 : Math.abs(String.valueOf(label).hashCode());
        return palette[Math.floorMod(index, palette.length)];
    }

    static boolean useDarkText(Context context, int slot, String label) {
        int color = courseColor(context, slot, label);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
        return luminance > 0.68;
    }

    static int lunchBackground(Context context) {
        return 0xFFF4F1EA;
    }

    static int lunchText(Context context) {
        return 0xFF54504A;
    }

    static int gapBackground(Context context) {
        return 0xFFF0F2F5;
    }

    static int gapText(Context context) {
        return 0xFF525C66;
    }

    private static int[] palette(Context context) {
        switch (getPalette(context)) {
            case "pastel":
                return new int[]{0xFFF58BA6,0xFFFFAD72,0xFFFFE3A0,0xFF8BD5A4,0xFF79D0D4,0xFF8CB7ED,0xFFB59AE7};
            case "warm":
                return new int[]{0xFFEF5968,0xFFFF7B72,0xFFFF9B59,0xFFF7B487,0xFFE6BF85,0xFFD98B9B,0xFFB98CA5};
            case "cool":
                return new int[]{0xFF3E91B8,0xFF42B6BE,0xFF4DB78B,0xFF84BF67,0xFF6DA3E7,0xFF6D82D7,0xFF9874D0};
            case "soft":
                return new int[]{0xFF7B8FA4,0xFF9AA7AF,0xFFCDD0BC,0xFF86A894,0xFF7AA7AA,0xFF8098B6,0xFF998DA9};
            default:
                return new int[]{0xFFF0335D,0xFFFF7B2F,0xFFFFE47D,0xFF21C877,0xFF18B9BE,0xFF2F83E8,0xFF9B55E9};
        }
    }

    private static boolean isValid(String id) {
        return "vivid".equals(id) || "pastel".equals(id) || "warm".equals(id)
                || "cool".equals(id) || "soft".equals(id);
    }
}
