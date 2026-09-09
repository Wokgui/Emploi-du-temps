package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

final class ProfileStore {
    private static final String PREFS = "schedule_profiles_v1";
    private static final String DATA = "profiles_json";
    private static final String CURRENT = "current_profile";
    private static final String MAIN = "main";

    private ProfileStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static synchronized void ensure(Context context) {
        try {
            JSONObject profiles = profiles(context);
            if (!profiles.has(MAIN)) {
                JSONObject p = new JSONObject();
                p.put("name", "Principal");
                p.put("schedule", ScheduleStore.exportJson(context));
                profiles.put(MAIN, p);
                saveProfiles(context, profiles);
            }
            if (prefs(context).getString(CURRENT, null) == null) {
                prefs(context).edit().putString(CURRENT, MAIN).apply();
            }
        } catch (Exception ignored) {}
    }

    static synchronized String listJson(Context context) {
        ensure(context);
        JSONObject root = new JSONObject();
        JSONArray arr = new JSONArray();
        try {
            String current = currentId(context);
            JSONObject profiles = profiles(context);
            Iterator<String> keys = profiles.keys();
            while (keys.hasNext()) {
                String id = keys.next();
                JSONObject p = profiles.optJSONObject(id);
                if (p == null) continue;
                JSONObject item = new JSONObject();
                item.put("id", id);
                item.put("name", p.optString("name", id));
                arr.put(item);
            }
            root.put("current", current);
            root.put("profiles", arr);
        } catch (Exception ignored) {}
        return root.toString();
    }

    static synchronized String create(Context context, String name, boolean duplicateCurrent) {
        ensure(context);
        saveCurrent(context);
        String id = "p_" + System.currentTimeMillis();
        try {
            JSONObject profiles = profiles(context);
            JSONObject p = new JSONObject();
            p.put("name", cleanName(name));
            p.put("schedule", duplicateCurrent ? ScheduleStore.exportJson(context) : blankSchedule(context));
            profiles.put(id, p);
            saveProfiles(context, profiles);
            return id;
        } catch (Exception ignored) {
            return MAIN;
        }
    }

    static synchronized String activate(Context context, String id) {
        ensure(context);
        try {
            JSONObject profiles = profiles(context);
            JSONObject target = profiles.optJSONObject(id);
            if (target == null) return ScheduleStore.exportJson(context);
            saveCurrent(context);
            prefs(context).edit().putString(CURRENT, id).apply();
            String schedule = target.optString("schedule", "{}");
            ScheduleStore.importJson(context, schedule);
            return ScheduleStore.exportJson(context);
        } catch (Exception ignored) {
            return ScheduleStore.exportJson(context);
        }
    }

    static synchronized void rename(Context context, String id, String name) {
        ensure(context);
        try {
            JSONObject profiles = profiles(context);
            JSONObject p = profiles.optJSONObject(id);
            if (p == null) return;
            p.put("name", cleanName(name));
            profiles.put(id, p);
            saveProfiles(context, profiles);
        } catch (Exception ignored) {}
    }

    static synchronized String delete(Context context, String id) {
        ensure(context);
        try {
            JSONObject profiles = profiles(context);
            if (profiles.length() <= 1 || !profiles.has(id)) return currentId(context);
            String current = currentId(context);
            if (id.equals(current)) {
                String replacement = null;
                Iterator<String> keys = profiles.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    if (!id.equals(k)) { replacement = k; break; }
                }
                if (replacement != null) activate(context, replacement);
            }
            profiles = profiles(context);
            profiles.remove(id);
            saveProfiles(context, profiles);
            return currentId(context);
        } catch (Exception ignored) {
            return currentId(context);
        }
    }

    static synchronized void saveCurrent(Context context) {
        ensure(context);
        try {
            String current = currentId(context);
            JSONObject profiles = profiles(context);
            JSONObject p = profiles.optJSONObject(current);
            if (p == null) {
                p = new JSONObject();
                p.put("name", current);
            }
            p.put("schedule", ScheduleStore.exportJson(context));
            profiles.put(current, p);
            saveProfiles(context, profiles);
        } catch (Exception ignored) {}
    }

    static synchronized String exportJson(Context context) {
        ensure(context);
        saveCurrent(context);
        JSONObject root = new JSONObject();
        try {
            root.put("current", currentId(context));
            root.put("profiles", profiles(context));
        } catch (Exception ignored) {}
        return root.toString();
    }

    static synchronized void importJson(Context context, String raw) {
        try {
            JSONObject root = new JSONObject(raw == null ? "{}" : raw);
            JSONObject profiles = root.optJSONObject("profiles");
            if (profiles == null || profiles.length() == 0) return;
            String current = root.optString("current", MAIN);
            if (!profiles.has(current)) {
                Iterator<String> keys = profiles.keys();
                if (keys.hasNext()) current = keys.next();
            }
            saveProfiles(context, profiles);
            prefs(context).edit().putString(CURRENT, current).apply();
            JSONObject p = profiles.optJSONObject(current);
            if (p != null) ScheduleStore.importJson(context, p.optString("schedule", "{}"));
        } catch (Exception ignored) {}
    }

    private static String currentId(Context context) {
        return prefs(context).getString(CURRENT, MAIN);
    }

    private static JSONObject profiles(Context context) {
        try {
            return new JSONObject(prefs(context).getString(DATA, "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static void saveProfiles(Context context, JSONObject profiles) {
        prefs(context).edit().putString(DATA, profiles.toString()).apply();
    }

    private static String cleanName(String name) {
        String s = name == null ? "Profil" : name.trim();
        return s.isEmpty() ? "Profil" : s.substring(0, Math.min(40, s.length()));
    }

    private static String blankSchedule(Context context) {
        try {
            JSONObject root = new JSONObject(ScheduleStore.exportJson(context));
            JSONArray enabled = new JSONArray(); for (int day = 2; day <= 6; day++) enabled.put(day); root.put("_enabledDays", enabled);
            JSONObject weeks = root.optJSONObject("_weeks");
            if (weeks != null) {
                for (String w : new String[]{"A","B","C","D"}) {
                    JSONObject week = weeks.optJSONObject(w);
                    if (week == null) continue;
                    for (int day : new int[]{2,3,4,5,6,7,1}) {
                        JSONObject d = week.optJSONObject(String.valueOf(day));
                        if (d != null) d.put("courses", new JSONArray());
                    }
                }
            }
            for (int day : new int[]{2,3,4,5,6,7,1}) {
                JSONObject d = root.optJSONObject(String.valueOf(day));
                if (d != null) d.put("courses", new JSONArray());
            }
            return root.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}
