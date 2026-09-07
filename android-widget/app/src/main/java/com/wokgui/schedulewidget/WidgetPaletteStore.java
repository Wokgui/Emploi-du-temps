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
        return palette(context)[0];
    }

    static int courseColor(Context context, int slot, String label) {
        return courseColor(context, slot, label, "");
    }

    static int courseColor(Context context, int slot, String label, String colorId) {
        int[] palette = palette(context);
        int custom = colorIndex(colorId);
        int index = custom >= 0 ? custom : (slot > 0 ? slot - 1 : Math.abs(String.valueOf(label).hashCode()));
        return palette[Math.floorMod(index, palette.length)];
    }

    static boolean useDarkText(Context context, int slot, String label) {
        return useDarkText(context, slot, label, "");
    }

    static boolean useDarkText(Context context, int slot, String label, String colorId) {
        return isLight(courseColor(context, slot, label, colorId));
    }

    static int lunchBackground(Context context) {
        int[] p = palette(context);
        return p[Math.min(2, p.length - 1)];
    }

    static int lunchText(Context context) {
        int bg = lunchBackground(context);
        return isLight(bg) ? 0xFF4B3B09 : 0xFFFFFFFF;
    }

    static int gapBackground(Context context) {
        int[] p = palette(context);
        return p[Math.min(6, p.length - 1)];
    }

    static int gapText(Context context) {
        int bg = gapBackground(context);
        return isLight(bg) ? 0xFF33294A : 0xFFFFFFFF;
    }

    private static boolean isLight(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b) / 255.0;
        return luminance > 0.68;
    }

    private static int colorIndex(String id) {
        if (id == null || id.isEmpty()) return -1;
        switch (id) {
            case "butter": case "blue": return 0;
            case "apricot": case "cyan": return 1;
            case "peach": case "teal": return 2;
            case "coral": case "green": return 3;
            case "terracotta": case "sand": case "yellow": return 4;
            case "rose": case "olive": case "orange": return 5;
            case "berry": case "violet": return 6;
            case "plum": case "red": case "graphite": return 6;
            default: return -1;
        }
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
