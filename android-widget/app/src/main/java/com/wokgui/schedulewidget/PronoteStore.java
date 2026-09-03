package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

final class PronoteStore {
    private static final String PREFS = "pronote_import_v1";
    private static final String SNAPSHOT = "snapshot";
    private static final String IMPORTED_AT = "imported_at";
    private static final String SOURCE_URL = "source_url";
    private static final String LAST_SYNC_ATTEMPT = "last_sync_attempt";
    private static final String LAST_SYNC_OK = "last_sync_ok";
    private static final String LAST_SYNC_MESSAGE = "last_sync_message";
    static final String DEFAULT_PRONOTE_URL = "https://0570107g.index-education.net/pronote/mobile.professeur.html";

    private PronoteStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void saveSnapshot(Context context, String payload) {
        mergeSnapshot(context, payload);
    }

    static synchronized void replaceSnapshot(Context context, String payload) {
        if (payload == null) return;
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) return;
        if (trimmed.length() > 900_000) trimmed = trimmed.substring(0, 900_000);
        try {
            JSONObject incoming = new JSONObject(trimmed);
            JSONArray cleaned = new JSONArray();
            appendBlocks(incoming.optJSONArray("blocks"), cleaned, new HashSet<>());
            JSONObject out = new JSONObject();
            out.put("url", incoming.optString("url", ""));
            out.put("title", incoming.optString("title", "PRONOTE"));
            out.put("capturedAt", System.currentTimeMillis());
            String text = incoming.optString("text", "");
            if (text.length() > 180_000) text = text.substring(0, 180_000);
            out.put("text", text);
            out.put("blocks", cleaned);
            writeSnapshot(context, out);
        } catch (Exception ignored) {
        }
    }

    static synchronized void mergeSnapshot(Context context, String payload) {
        if (payload == null) return;
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) return;
        if (trimmed.length() > 900_000) trimmed = trimmed.substring(0, 900_000);

        try {
            JSONObject incoming = new JSONObject(trimmed);
            JSONObject previous;
            try {
                previous = new JSONObject(getSnapshot(context));
            } catch (Exception ignored) {
                previous = new JSONObject();
            }

            JSONArray merged = new JSONArray();
            Set<String> seen = new HashSet<>();
            appendBlocks(previous.optJSONArray("blocks"), merged, seen);
            appendBlocks(incoming.optJSONArray("blocks"), merged, seen);

            JSONObject out = new JSONObject();
            out.put("url", incoming.optString("url", previous.optString("url", "")));
            out.put("title", incoming.optString("title", previous.optString("title", "PRONOTE")));
            out.put("capturedAt", System.currentTimeMillis());
            String text = incoming.optString("text", "");
            if (text.isEmpty()) text = previous.optString("text", "");
            if (text.length() > 180_000) text = text.substring(0, 180_000);
            out.put("text", text);
            out.put("blocks", merged);
            writeSnapshot(context, out);
        } catch (Exception ignored) {
        }
    }

    private static void writeSnapshot(Context context, JSONObject out) {
        String sourceUrl = out.optString("url", "");
        prefs(context).edit()
                .putString(SNAPSHOT, out.toString())
                .putLong(IMPORTED_AT, System.currentTimeMillis())
                .putString(SOURCE_URL, sourceUrl)
                .apply();
    }

    private static void appendBlocks(JSONArray source, JSONArray target, Set<String> seen) {
        if (source == null) return;
        for (int i = 0; i < source.length() && target.length() < 160; i++) {
            JSONObject block = source.optJSONObject(i);
            if (block == null) continue;
            String text = block.optString("text", "");
            String context = block.optString("context", "");
            String key = normalize(context + " " + text);
            if (key.isEmpty() || seen.contains(key)) continue;
            seen.add(key);
            target.put(block);
        }
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String n = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");
        return n.length() > 900 ? n.substring(0, 900) : n;
    }

    static String getSnapshot(Context context) {
        return prefs(context).getString(SNAPSHOT, "{}");
    }

    static long getImportedAt(Context context) {
        return prefs(context).getLong(IMPORTED_AT, 0L);
    }

    static long getLastSyncAttempt(Context context) {
        return prefs(context).getLong(LAST_SYNC_ATTEMPT, 0L);
    }

    static void markSyncAttempt(Context context) {
        prefs(context).edit()
                .putLong(LAST_SYNC_ATTEMPT, System.currentTimeMillis())
                .putBoolean(LAST_SYNC_OK, false)
                .putString(LAST_SYNC_MESSAGE, "Synchronisation en cours")
                .apply();
    }

    static void markSyncSuccess(Context context, String message) {
        prefs(context).edit()
                .putLong(LAST_SYNC_ATTEMPT, System.currentTimeMillis())
                .putBoolean(LAST_SYNC_OK, true)
                .putString(LAST_SYNC_MESSAGE, message == null ? "Synchronisé" : message)
                .apply();
    }

    static void markSyncError(Context context, String message) {
        prefs(context).edit()
                .putLong(LAST_SYNC_ATTEMPT, System.currentTimeMillis())
                .putBoolean(LAST_SYNC_OK, false)
                .putString(LAST_SYNC_MESSAGE, message == null ? "Synchronisation impossible" : message)
                .apply();
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
            out.put("lastSyncAttempt", getLastSyncAttempt(context));
            out.put("lastSyncOk", prefs(context).getBoolean(LAST_SYNC_OK, false));
            out.put("lastSyncMessage", prefs(context).getString(LAST_SYNC_MESSAGE, ""));
        } catch (Exception ignored) {
        }
        return out.toString();
    }

    static void clear(Context context) {
        prefs(context).edit().clear().apply();
    }
}
