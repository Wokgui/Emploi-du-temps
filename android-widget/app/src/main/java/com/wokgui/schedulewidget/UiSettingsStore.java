package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Locale;

final class UiSettingsStore {
    private static final String PREFS = "ui_settings_v1";
    private static final String APP_FONT = "app_font_scale";
    private static final String WIDGET_FONT = "widget_font_scale";
    private static final String LANGUAGE = "language";
    private static final String THEME = "theme";

    private UiSettingsStore() {}

    static final class Theme {
        final String id;
        final int accent;
        final int accentDark;
        final int widgetCard;
        final int widgetSurface;
        final int ink;
        final int muted;
        final int progressTrack;

        Theme(String id, int accent, int accentDark, int widgetCard, int widgetSurface,
              int ink, int muted, int progressTrack) {
            this.id = id;
            this.accent = accent;
            this.accentDark = accentDark;
            this.widgetCard = widgetCard;
            this.widgetSurface = widgetSurface;
            this.ink = ink;
            this.muted = muted;
            this.progressTrack = progressTrack;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static float appFontScale(Context context) {
        return clampScale(prefs(context).getFloat(APP_FONT, 1.0f));
    }

    static float widgetFontScale(Context context) {
        return clampScale(prefs(context).getFloat(WIDGET_FONT, 1.0f));
    }

    static String language(Context context) {
        String value = prefs(context).getString(LANGUAGE, "fr");
        if ("de".equals(value) || "en".equals(value)) return value;
        return "fr";
    }

    private static boolean supportedTheme(String value) {
        return "blue".equals(value) || "teal".equals(value) || "violet".equals(value)
                || "green".equals(value) || "amber".equals(value) || "rose".equals(value)
                || "red".equals(value) || "indigo".equals(value) || "cyan".equals(value)
                || "coral".equals(value) || "navy".equals(value) || "graphite".equals(value);
    }

    static String themeId(Context context) {
        String value = prefs(context).getString(THEME, "blue");
        return supportedTheme(value) ? value : "blue";
    }

    static Theme theme(Context context) {
        return theme(themeId(context));
    }

    static Theme theme(String id) {
        switch (id == null ? "blue" : id) {
            case "teal":
                return new Theme("teal", 0xFF00897B, 0xFF00695C, 0xFFDDF4F0,
                        0xFFFFFFFF, 0xFF102B28, 0xFF4F6965, 0xFFB9D6D1);
            case "violet":
                return new Theme("violet", 0xFF6750A4, 0xFF4F378B, 0xFFEDE7F6,
                        0xFFFFFFFF, 0xFF241B35, 0xFF665D72, 0xFFD0C5E1);
            case "green":
                return new Theme("green", 0xFF2E7D32, 0xFF1B5E20, 0xFFE3F3E4,
                        0xFFFFFFFF, 0xFF172B19, 0xFF5D705F, 0xFFBDD7BF);
            case "amber":
                return new Theme("amber", 0xFFEF6C00, 0xFFBF4E00, 0xFFFFEBD8,
                        0xFFFFFFFF, 0xFF352015, 0xFF756256, 0xFFE3C7AC);
            case "rose":
                return new Theme("rose", 0xFFD81B60, 0xFFAD1457, 0xFFFCE4EC,
                        0xFFFFFFFF, 0xFF341723, 0xFF75616A, 0xFFE0BEC9);
            case "red":
                return new Theme("red", 0xFFD84343, 0xFFB72E2E, 0xFFFDE8E8,
                        0xFFFFFFFF, 0xFF351919, 0xFF776060, 0xFFE2BDBD);
            case "indigo":
                return new Theme("indigo", 0xFF3F51B5, 0xFF303F9F, 0xFFE8EAF6,
                        0xFFFFFFFF, 0xFF1D2342, 0xFF666A7D, 0xFFC6CAE4);
            case "cyan":
                return new Theme("cyan", 0xFF0097A7, 0xFF007C91, 0xFFE0F7FA,
                        0xFFFFFFFF, 0xFF123036, 0xFF60757A, 0xFFB9DDE2);
            case "coral":
                return new Theme("coral", 0xFFE76F51, 0xFFC95035, 0xFFFCE9E3,
                        0xFFFFFFFF, 0xFF3A211B, 0xFF79675F, 0xFFE6C3B8);
            case "navy":
                return new Theme("navy", 0xFF2457A7, 0xFF193E7A, 0xFFE5EDFA,
                        0xFFFFFFFF, 0xFF17243B, 0xFF617086, 0xFFC0CEE2);
            case "graphite":
                return new Theme("graphite", 0xFF546E7A, 0xFF37474F, 0xFFECEFF1,
                        0xFFFFFFFF, 0xFF1D292E, 0xFF68757B, 0xFFC9D1D5);
            default:
                return new Theme("blue", 0xFF0877F9, 0xFF075FAE, 0xFFD9EAFB,
                        0xFFFFFFFF, 0xFF101936, 0xFF465369, 0xFFB9CCE1);
        }
    }

    static String exportJson(Context context) {
        try {
            JSONObject o = new JSONObject();
            o.put("appFontScale", appFontScale(context));
            o.put("widgetFontScale", widgetFontScale(context));
            o.put("language", language(context));
            o.put("theme", themeId(context));
            return o.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    static void importJson(Context context, String json) {
        try {
            JSONObject o = new JSONObject(json == null ? "{}" : json);
            float appScale = clampScale((float) o.optDouble("appFontScale", appFontScale(context)));
            float widgetScale = clampScale((float) o.optDouble("widgetFontScale", widgetFontScale(context)));
            String lang = o.optString("language", language(context));
            if (!"de".equals(lang) && !"en".equals(lang)) lang = "fr";
            String theme = o.optString("theme", themeId(context));
            if (!supportedTheme(theme)) theme = "blue";

            prefs(context).edit()
                    .putFloat(APP_FONT, appScale)
                    .putFloat(WIDGET_FONT, widgetScale)
                    .putString(LANGUAGE, lang)
                    .putString(THEME, theme)
                    .apply();
            ScheduleStore.refreshWidgets(context);
        } catch (Exception ignored) {
        }
    }

    static Locale locale(Context context) {
        switch (language(context)) {
            case "de": return Locale.GERMANY;
            case "en": return Locale.UK;
            default: return Locale.FRANCE;
        }
    }

    static String t(Context context, String key) {
        return t(language(context), key);
    }

    static String t(String lang, String key) {
        if ("de".equals(lang)) {
            switch (key) {
                case "next": return "Nächste Stunde";
                case "current": return "● Läuft";
                case "room": return "Raum";
                case "noCourse": return "Kein Unterricht geplant";
                case "gap": return "Freistunde";
                case "lunch": return "Mittagspause";
                case "backAt": return "Weiter um";
                case "noClass": return "kein Unterricht";
                case "in": return "In";
                case "tomorrow": return "Morgen";
                case "remainingMinute": return "Min. übrig";
                case "remainingMinutes": return "Min. übrig";
                case "remainingHour": return "Std. übrig";
                case "remainingHours": return "Std. übrig";
                case "courseEnd": return "Stundenende";
                default: return key;
            }
        }
        if ("en".equals(lang)) {
            switch (key) {
                case "next": return "Next class";
                case "current": return "● In class";
                case "room": return "room";
                case "noCourse": return "No class scheduled";
                case "gap": return "Free period";
                case "lunch": return "Lunch break";
                case "backAt": return "Back at";
                case "noClass": return "no class";
                case "in": return "In";
                case "tomorrow": return "Tomorrow";
                case "remainingMinute": return "min left";
                case "remainingMinutes": return "min left";
                case "remainingHour": return "h left";
                case "remainingHours": return "h left";
                case "courseEnd": return "End of class";
                default: return key;
            }
        }
        switch (key) {
            case "next": return "Prochain cours";
            case "current": return "● En cours";
            case "room": return "salle";
            case "noCourse": return "Aucun cours programmé";
            case "gap": return "Trou";
            case "lunch": return "Pause de midi";
            case "backAt": return "Reprise à";
            case "noClass": return "sans cours";
            case "in": return "Dans";
            case "tomorrow": return "Demain";
            case "remainingMinute": return "min restante";
            case "remainingMinutes": return "min restantes";
            case "remainingHour": return "h restante";
            case "remainingHours": return "h restantes";
            case "courseEnd": return "Fin du cours";
            default: return key;
        }
    }

    private static float clampScale(float value) {
        return Math.max(0.80f, Math.min(1.40f, value));
    }
}
