package com.wokgui.schedulewidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class ScheduleStore {
    private static final String PREFS = "schedule_store_v1";
    private static final String INIT = "initialized";

    private static final String[] DEFAULT_START = {
            "08:00","09:00","10:00","11:00","13:00","14:00","16:00"
    };
    private static final String[] DEFAULT_END = {
            "09:00","10:00","11:00","12:00","14:00","15:00","17:00"
    };

    private ScheduleStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void ensureInitialized(Context context) {
        SharedPreferences p = prefs(context);
        SharedPreferences.Editor e = p.edit();
        if (!p.getBoolean(INIT, false)) {
            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                e.putBoolean("enabled_" + day, true);
                e.putString("day_" + day, encode(ScheduleData.defaultForDay(day)));
            }
            e.putBoolean(INIT, true);
        }
        for (int i = 0; i < 7; i++) {
            if (!p.contains("slot_" + (i + 1) + "_start")) {
                e.putString("slot_" + (i + 1) + "_start", DEFAULT_START[i]);
            }
            if (!p.contains("slot_" + (i + 1) + "_end")) {
                e.putString("slot_" + (i + 1) + "_end", DEFAULT_END[i]);
            }
        }
        for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
            e.putBoolean("enabled_" + day, true);
        }
        e.apply();
    }

    static boolean isDayEnabled(Context context, int day) {
        ensureInitialized(context);
        return true;
    }

    static String getSlotStart(Context context, int slot) {
        ensureInitialized(context);
        int i = Math.max(1, Math.min(7, slot)) - 1;
        return prefs(context).getString("slot_" + (i + 1) + "_start", DEFAULT_START[i]);
    }

    static String getSlotEnd(Context context, int slot) {
        ensureInitialized(context);
        int i = Math.max(1, Math.min(7, slot)) - 1;
        return prefs(context).getString("slot_" + (i + 1) + "_end", DEFAULT_END[i]);
    }

    static List<ScheduleData.Course> getStoredCourses(Context context, int day) {
        ensureInitialized(context);
        String json = prefs(context).getString("day_" + day, "[]");
        List<ScheduleData.Course> result = decode(json);
        Collections.sort(result, Comparator.comparingInt(c -> ScheduleData.toMinutes(c.start)));
        return result;
    }

    static List<ScheduleData.Course> getCourses(Context context, int day) {
        return getStoredCourses(context, day);
    }

    static String exportJson(Context context) {
        ensureInitialized(context);
        try {
            JSONObject root = new JSONObject();
            JSONArray slots = new JSONArray();
            for (int i = 1; i <= 7; i++) {
                JSONObject slot = new JSONObject();
                slot.put("start", getSlotStart(context, i));
                slot.put("end", getSlotEnd(context, i));
                slots.put(slot);
            }
            root.put("_slots", slots);

            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                JSONObject d = new JSONObject();
                d.put("enabled", true);
                d.put("courses", new JSONArray(encode(getStoredCourses(context, day))));
                root.put(String.valueOf(day), d);
            }
            return root.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    static void importJson(Context context, String json) {
        ensureInitialized(context);
        try {
            JSONObject root = new JSONObject(json);
            SharedPreferences.Editor editor = prefs(context).edit();

            JSONArray slots = root.optJSONArray("_slots");
            if (slots != null) {
                for (int i = 0; i < Math.min(7, slots.length()); i++) {
                    JSONObject s = slots.optJSONObject(i);
                    if (s == null) continue;
                    String start = s.optString("start", DEFAULT_START[i]);
                    String end = s.optString("end", DEFAULT_END[i]);
                    editor.putString("slot_" + (i + 1) + "_start", start);
                    editor.putString("slot_" + (i + 1) + "_end", end);
                }
            }

            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                JSONObject d = root.optJSONObject(String.valueOf(day));
                editor.putBoolean("enabled_" + day, true);
                if (d == null) continue;
                JSONArray arr = d.optJSONArray("courses");
                if (arr != null) editor.putString("day_" + day, arr.toString());
            }
            editor.apply();
            refreshWidgets(context);
        } catch (Exception ignored) {
        }
    }

    static void refreshWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, ScheduleWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        Intent refresh = new Intent(context, ScheduleWidgetProvider.class)
                .setAction(ScheduleWidgetProvider.ACTION_REFRESH);
        context.sendBroadcast(refresh);
    }

    private static String encode(List<ScheduleData.Course> courses) {
        JSONArray arr = new JSONArray();
        try {
            for (ScheduleData.Course c : courses) {
                JSONObject o = new JSONObject();
                o.put("start", c.start);
                o.put("end", c.end);
                o.put("label", c.label);
                o.put("room", c.room);
                o.put("slot", c.slot);
                arr.put(o);
            }
        } catch (Exception ignored) {
        }
        return arr.toString();
    }

    private static List<ScheduleData.Course> decode(String json) {
        List<ScheduleData.Course> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json == null ? "[]" : json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String start = o.optString("start", "08:00");
                String end = o.optString("end", "09:00");
                String label = o.optString("label", "Cours");
                String room = o.optString("room", "");
                int slot = o.optInt("slot", 0);
                if (slot == 0) {
                    for (int n = 0; n < 7; n++) {
                        if (DEFAULT_START[n].equals(start) && DEFAULT_END[n].equals(end)) {
                            slot = n + 1;
                            break;
                        }
                    }
                }
                out.add(new ScheduleData.Course(start, end, label, room, slot));
            }
        } catch (Exception ignored) {
        }
        return out;
    }
}
