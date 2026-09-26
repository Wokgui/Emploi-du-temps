package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import org.json.JSONObject;

final class WidgetPaletteStore {
    private static final String PREFS = "widget_palette_v1";
    private static final String KEY = "palette";
    private static final String SPECIAL = "special_colors_v2";
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
        Integer literal = literalColor(colorId);
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) return 0xFFFFFFFF;
        if ("fill".equals(AdvancedSettingsStore.widgetClassColorMode(context))) return assignedCourseColor(context, slot, label, colorId);
        if (literal != null) return literal;
        int[] palette = palette(context);
        int custom = colorIndex(colorId);
        int index = custom >= 0 ? custom : (slot > 0 ? slot - 1 : Math.abs(String.valueOf(label).hashCode()));
        return palette[Math.floorMod(index, palette.length)];
    }

    static int assignedCourseColor(Context context, int slot, String label, String colorId) {
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) return 0xFF202020;
        Integer literal = literalColor(colorId);
        if (literal != null) return literal;
        int[] colors = palette(context);
        int custom = colorIndex(colorId);
        if (custom >= 0) return colors[Math.min(custom, colors.length - 1)];
        return AdvancedSettingsStore.classColor(context, label,
                colors[Math.floorMod(slot > 0 ? slot - 1 : Math.abs(String.valueOf(label).hashCode()), colors.length)]);
    }

    static int courseBackground(Context context, int slot, String label, String colorId) {
        return "stripe".equals(AdvancedSettingsStore.widgetClassColorMode(context))
                ? 0xFFFFFFFF : courseColor(context, slot, label, colorId);
    }

    static boolean useDarkTextForColor(int color) { return isLight(color); }

    static boolean useDarkText(Context context, int slot, String label) {
        return useDarkText(context, slot, label, "");
    }

    static boolean useDarkText(Context context, int slot, String label, String colorId) {
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) return true;
        return isLight(courseColor(context, slot, label, colorId));
    }

    private static JSONObject specialDefaults() {
        JSONObject o = new JSONObject();
        try {
            o.put("sync", true);
            // Des bandes colorées mais plus douces que les cours : pas de gris ni de blanc brut.
            o.put("appLunch", "#FFE4A8");
            o.put("appGap", "#DDF2EC");
            o.put("widgetLunch", "#FFE4A8");
            o.put("widgetGap", "#DDF2EC");
        } catch (Exception ignored) {}
        return o;
    }

    static synchronized JSONObject special(Context context) {
        JSONObject out = specialDefaults();
        try {
            String raw = prefs(context).getString(SPECIAL, null);
            if (raw != null) {
                JSONObject saved = new JSONObject(raw);
                if (saved.has("sync")) out.put("sync", saved.optBoolean("sync", true));
                for (String key : new String[]{"appLunch","appGap","widgetLunch","widgetGap"}) {
                    String value = normalizeHex(saved.optString(key, out.optString(key)));
                    out.put(key, value);
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    static synchronized String specialColorsJson(Context context) {
        return special(context).toString();
    }

    static synchronized void saveSpecialColorsJson(Context context, String raw) {
        try {
            JSONObject incoming = new JSONObject(raw == null ? "{}" : raw);
            JSONObject out = special(context);
            boolean sync = incoming.optBoolean("sync", out.optBoolean("sync", true));
            out.put("sync", sync);
            for (String key : new String[]{"appLunch","appGap","widgetLunch","widgetGap"}) {
                if (incoming.has(key)) out.put(key, normalizeHex(incoming.optString(key, out.optString(key))));
            }
            if (sync) {
                out.put("widgetLunch", out.optString("appLunch", "#FFE4A8"));
                out.put("widgetGap", out.optString("appGap", "#DDF2EC"));
            }
            prefs(context).edit().putString(SPECIAL, out.toString()).apply();
        } catch (Exception ignored) {}
    }

    static synchronized String exportJson(Context context) {
        JSONObject out = new JSONObject();
        try {
            out.put("palette", getPalette(context));
            out.put("special", special(context));
        } catch (Exception ignored) {}
        return out.toString();
    }

    static synchronized void importJson(Context context, String raw) {
        try {
            JSONObject incoming = new JSONObject(raw == null ? "{}" : raw);
            setPalette(context, incoming.optString("palette", DEFAULT));
            JSONObject colors = incoming.optJSONObject("special");
            if (colors != null) saveSpecialColorsJson(context, colors.toString());
        } catch (Exception ignored) {}
    }

    static int lunchBackground(Context context) {
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) return 0xFFFFFFFF;
        return parseOr(AdvancedSettingsStore.weekLunchColor(context), 0xFFFFE08A);
    }

    static int lunchText(Context context) {
        return isLight(lunchBackground(context)) ? 0xFF22283A : 0xFFFFFFFF;
    }

    static int gapBackground(Context context) {
        if ("high_contrast".equals(AdvancedSettingsStore.accessibility(context))) return 0xFFFFFFFF;
        return parseOr(AdvancedSettingsStore.weekFreeColor(context), 0xFFEAF4FF);
    }

    static int gapText(Context context) {
        return isLight(gapBackground(context)) ? 0xFF22283A : 0xFFFFFFFF;
    }

    private static int parseOr(String value, int fallback) {
        try { return Color.parseColor(normalizeHex(value)); }
        catch (Exception ignored) { return fallback; }
    }

    private static Integer literalColor(String id) {
        if (id == null) return null;
        String s = id.trim();
        if (!s.matches("#[0-9A-Fa-f]{6}")) return null;
        try { return Color.parseColor(s); }
        catch (Exception ignored) { return null; }
    }

    private static String normalizeHex(String value) {
        String s = value == null ? "" : value.trim();
        if (s.matches("#[0-9A-Fa-f]{6}")) return s.toUpperCase();
        return "#FFFFFF";
    }

    private static boolean isLight(int color) {
        return contrast(color, 0xFF17213A) >= contrast(color, 0xFFFFFFFF);
    }

    private static double contrast(int first, int second) {
        double a = luminance(first), b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(int color) {
        double r = channel((color >> 16) & 0xFF);
        double g = channel((color >> 8) & 0xFF);
        double b = channel(color & 0xFF);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
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

    static void reset(Context context) {
        prefs(context).edit().clear().commit();
    }
}
