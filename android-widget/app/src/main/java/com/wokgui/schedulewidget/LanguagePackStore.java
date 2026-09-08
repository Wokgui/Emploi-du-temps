package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

final class LanguagePackStore {
    private static final String PREFS = "language_packs_v1";
    private LanguagePackStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean save(Context context, String json) {
        try {
            JSONObject root = new JSONObject(json == null ? "{}" : json);
            String code = root.optString("code", "").trim();
            String name = root.optString("name", "").trim();
            if (!code.matches("[A-Za-z]{2,3}(?:[-_][A-Za-z]{2,4})?") || name.isEmpty()) return false;
            JSONObject strings = root.optJSONObject("strings");
            if (strings == null || strings.length() == 0) return false;
            prefs(context).edit().putString("pack_" + code, root.toString()).apply();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static boolean has(Context context, String code) {
        if (code == null || code.isEmpty()) return false;
        return prefs(context).contains("pack_" + code);
    }

    static String get(Context context, String code) {
        return prefs(context).getString("pack_" + code, "");
    }

    static String listJson(Context context) {
        JSONArray out = new JSONArray();
        try {
            for (String key : prefs(context).getAll().keySet()) {
                if (!key.startsWith("pack_")) continue;
                String raw = prefs(context).getString(key, "");
                if (raw == null || raw.isEmpty()) continue;
                JSONObject root = new JSONObject(raw);
                JSONObject item = new JSONObject();
                item.put("code", root.optString("code", key.substring(5)));
                item.put("name", root.optString("name", key.substring(5)));
                item.put("version", root.optInt("version", 1));
                out.put(item);
            }
        } catch (Exception ignored) {}
        return out.toString();
    }

    static String widgetText(Context context, String code, String key) {
        try {
            String raw = get(context, code);
            if (raw == null || raw.isEmpty()) return null;
            JSONObject root = new JSONObject(raw);
            JSONObject widget = root.optJSONObject("widget");
            if (widget == null) return null;
            String value = widget.optString(key, "").trim();
            return value.isEmpty() ? null : value;
        } catch (Exception e) {
            return null;
        }
    }
}
