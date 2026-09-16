package com.wokgui.schedulewidget;

import android.content.Context;

import org.json.JSONObject;

final class BackupStore {
    private BackupStore() {}

    static String exportJson(Context context) {
        JSONObject root = new JSONObject();
        try {
            ProfileStore.saveCurrent(context);
            root.put("format", "emploi-du-temps-backup");
            root.put("version", 2);
            root.put("schedule", new JSONObject(ScheduleStore.exportJson(context)));
            root.put("ui", new JSONObject(UiSettingsStore.exportJson(context)));
            root.put("advanced", new JSONObject(AdvancedSettingsStore.exportJson(context)));
            root.put("profiles", new JSONObject(ProfileStore.exportJson(context)));
            root.put("widgetPalette", new JSONObject(WidgetPaletteStore.exportJson(context)));
            root.put("languagePacks", new org.json.JSONArray(LanguagePackStore.exportJson(context)));
        } catch (Exception ignored) {}
        return root.toString();
    }

    static boolean importJson(Context context, String raw) {
        try {
            JSONObject root = new JSONObject(raw == null ? "{}" : raw);
            if (!"emploi-du-temps-backup".equals(root.optString("format", ""))) return false;
            org.json.JSONArray languagePacks = root.optJSONArray("languagePacks");
            if (languagePacks != null) LanguagePackStore.importJson(context, languagePacks.toString());
            JSONObject widgetPalette = root.optJSONObject("widgetPalette");
            if (widgetPalette != null) WidgetPaletteStore.importJson(context, widgetPalette.toString());
            JSONObject ui = root.optJSONObject("ui");
            if (ui != null) UiSettingsStore.importJson(context, ui.toString());
            JSONObject advanced = root.optJSONObject("advanced");
            if (advanced != null) AdvancedSettingsStore.importJson(context, advanced.toString());
            JSONObject profiles = root.optJSONObject("profiles");
            if (profiles != null) ProfileStore.importJson(context, profiles.toString());
            else {
                JSONObject schedule = root.optJSONObject("schedule");
                if (schedule != null) ScheduleStore.importJson(context, schedule.toString());
            }
            ScheduleStore.refreshWidgets(context);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
