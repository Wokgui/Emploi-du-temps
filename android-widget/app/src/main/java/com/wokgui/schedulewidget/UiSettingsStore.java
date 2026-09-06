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

    static String themeId(Context context) {
        String value = prefs(context).getString(THEME, "blue");
        if ("teal".equals(value) || "violet".equals(value)
                || "green".equals(value) || "amber".equals(value)) return value;
        return "blue";
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
            if (!"teal".equals(theme) && !"violet".equals(theme)
                    && !"green".equals(theme) && !"amber".equals(theme)) theme = "blue";

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
