package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

final class PronoteStore {
    private static final String PREFS = "pronote_import_v1";
    private static final String SNAPSHOT = "snapshot";
    private static final String IMPORTED_AT = "imported_at";
    private static final String SOURCE_URL = "source_url";
    static final String DEFAULT_PRONOTE_URL = "https://0570107g.index-education.net/pronote/mobile.professeur.html";

    private PronoteStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void saveSnapshot(Context context, String payload) {
        if (payload == null) return;
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) return;
        // On garde un plafond raisonnable pour ne pas faire grossir les préférences.
        if (trimmed.length() > 900_000) trimmed = trimmed.substring(0, 900_000);
        String sourceUrl = "";
        try {
            JSONObject object = new JSONObject(trimmed);
            sourceUrl = object.optString("url", "");
        } catch (Exception ignored) {
        }
        prefs(context).edit()
                .putString(SNAPSHOT, trimmed)
                .putLong(IMPORTED_AT, System.currentTimeMillis())
                .putString(SOURCE_URL, sourceUrl)
                .apply();
    }

    static String getSnapshot(Context context) {
        return prefs(context).getString(SNAPSHOT, "{}");
    }

    static long getImportedAt(Context context) {
        return prefs(context).getLong(IMPORTED_AT, 0L);
    }

    static String getStatusJson(Context context) {
        JSONObject out = new JSONObject();
        try {
            long importedAt = getImportedAt(context);
            String snapshot = getSnapshot(context);
            JSONObject data = new JSONObject(snapshot == null || snapshot.isEmpty() ? "{}" : snapshot);
            JSONArray blocks = data.optJSONArray("blocks");
            out.put("hasData", importedAt > 0L);
            out.put("importedAt", importedAt);
            out.put("blocks", blocks == null ? 0 : blocks.length());
            out.put("title", data.optString("title", ""));
            out.put("url", prefs(context).getString(SOURCE_URL, ""));
        } catch (Exception ignored) {
        }
        return out.toString();
    }

    static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }
}
