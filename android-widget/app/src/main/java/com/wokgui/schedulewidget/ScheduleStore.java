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

    private ScheduleStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static void ensureInitialized(Context context) {
        SharedPreferences p = prefs(context);
        if (p.getBoolean(INIT, false)) return;
        SharedPreferences.Editor e = p.edit();
        for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
            e.putBoolean("enabled_" + day, true);
            e.putString("day_" + day, encode(ScheduleData.defaultForDay(day)));
        }
        e.putBoolean(INIT, true).apply();
    }

    static boolean isDayEnabled(Context context, int day) {
        ensureInitialized(context);
        return prefs(context).getBoolean("enabled_" + day, true);
    }

    static List<ScheduleData.Course> getStoredCourses(Context context, int day) {
        ensureInitialized(context);
        String json = prefs(context).getString("day_" + day, "[]");
        List<ScheduleData.Course> result = decode(json);
        Collections.sort(result, Comparator.comparingInt(c -> ScheduleData.toMinutes(c.start)));
        return result;
    }

    static List<ScheduleData.Course> getCourses(Context context, int day) {
        if (!isDayEnabled(context, day)) return Collections.emptyList();
        return getStoredCourses(context, day);
    }

    static String exportJson(Context context) {
        ensureInitialized(context);
        try {
            JSONObject root = new JSONObject();
            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                JSONObject d = new JSONObject();
                d.put("enabled", isDayEnabled(context, day));
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
            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                JSONObject d = root.optJSONObject(String.valueOf(day));
                if (d == null) continue;
                editor.putBoolean("enabled_" + day, d.optBoolean("enabled", true));
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
        Intent refresh = new Intent(context, ScheduleWidgetProvider.class).setAction(ScheduleWidgetProvider.ACTION_REFRESH);
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
                out.add(new ScheduleData.Course(start, end, label, room));
            }
        } catch (Exception ignored) {
        }
        return out;
    }
}
