package com.wokgui.schedulewidget;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class AdvancedSettingsStore {
    private static final String PREFS = "advanced_settings_v1";
    private static final String JSON = "settings_json";

    private AdvancedSettingsStore() {}

    private static JSONObject defaults() {
        JSONObject o = new JSONObject();
        try {
            o.put("density", "normal");
            o.put("upcomingCount", 0);
            o.put("widgetFormat", "timeline");
            o.put("showRoom", true);
            o.put("showTimes", true);
            o.put("showRemaining", true);
            o.put("showPercent", true);
            o.put("showProgress", true);
            o.put("showBreaks", true);
            o.put("showLunch", true);
            o.put("showWeekInfo", true);
            o.put("colorByClass", false);
            o.put("accessibility", "normal");
            o.put("cycleLength", 2);
            o.put("singleWeek", false);
            o.put("remindersEnabled", false);
            o.put("reminderMinutes", 10);
            o.put("holidayMode", "alsace_moselle");
            o.put("exceptions", new JSONArray());
            o.put("dayOffRanges", new JSONArray());
            o.put("gapWidgetLabel", "");
            o.put("lunchWidgetLabel", "");
            o.put("widgetCourseLabels", new JSONObject());
        } catch (Exception ignored) {}
        return o;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static synchronized JSONObject json(Context context) {
        JSONObject base = defaults();
        try {
            String raw = prefs(context).getString(JSON, null);
            if (raw != null) {
                JSONObject saved = new JSONObject(raw);
                JSONArray names = saved.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.optString(i);
                        base.put(key, saved.opt(key));
                    }
                }
            }
        } catch (Exception ignored) {}
        return base;
    }

    static synchronized String exportJson(Context context) { return json(context).toString(); }

    static synchronized void importJson(Context context, String raw) {
        try {
            JSONObject incoming = new JSONObject(raw == null ? "{}" : raw);
            JSONObject merged = defaults();
            JSONArray names = incoming.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String key = names.optString(i);
                    merged.put(key, incoming.opt(key));
                }
            }
            normalize(merged);
            prefs(context).edit().putString(JSON, merged.toString()).apply();
            ScheduleStore.refreshWidgets(context);
            ReminderScheduler.reschedule(context);
        } catch (Exception ignored) {}
    }

    static synchronized void setCycleLength(Context context, int length) {
        try {
            JSONObject o = json(context);
            o.put("cycleLength", clamp(length, 2, 4));
            prefs(context).edit().putString(JSON, o.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static void normalize(JSONObject merged) throws Exception {
        merged.put("cycleLength", clamp(merged.optInt("cycleLength", 2), 2, 4));
        merged.put("upcomingCount", clamp(merged.optInt("upcomingCount", 0), 0, 6));
        merged.put("reminderMinutes", clamp(merged.optInt("reminderMinutes", 10), 0, 120));
        String density = merged.optString("density", "normal");
        if (!"compact".equals(density) && !"comfortable".equals(density)) density = "normal";
        merged.put("density", density);
        String format = merged.optString("widgetFormat", "timeline");
        if (!"compact".equals(format)) format = "timeline";
        merged.put("widgetFormat", format);
        String access = merged.optString("accessibility", "normal");
        if (!"high_contrast".equals(access) && !"colorblind".equals(access)) access = "normal";
        merged.put("accessibility", access);
        String holiday = merged.optString("holidayMode", "alsace_moselle");
        if (!"off".equals(holiday) && !"france".equals(holiday)) holiday = "alsace_moselle";
        merged.put("holidayMode", holiday);
        if (!(merged.opt("exceptions") instanceof JSONArray)) merged.put("exceptions", new JSONArray());
        if (!(merged.opt("dayOffRanges") instanceof JSONArray)) merged.put("dayOffRanges", new JSONArray());
        if (!(merged.opt("widgetCourseLabels") instanceof JSONObject)) merged.put("widgetCourseLabels", new JSONObject());
        merged.put("gapWidgetLabel", merged.optString("gapWidgetLabel", "").trim());
        merged.put("lunchWidgetLabel", merged.optString("lunchWidgetLabel", "").trim());
    }

    static int cycleLength(Context context) { return clamp(json(context).optInt("cycleLength", 2), 2, 4); }
    static String density(Context context) { return json(context).optString("density", "normal"); }
    static int upcomingCount(Context context) { return clamp(json(context).optInt("upcomingCount", 0), 0, 6); }
    static String widgetFormat(Context context) { return json(context).optString("widgetFormat", "timeline"); }
    static boolean showRoom(Context context) { return json(context).optBoolean("showRoom", true); }
    static boolean showTimes(Context context) { return json(context).optBoolean("showTimes", true); }
    static boolean showRemaining(Context context) { return json(context).optBoolean("showRemaining", true); }
    static boolean showPercent(Context context) { return json(context).optBoolean("showPercent", true); }
    static boolean showProgress(Context context) { return json(context).optBoolean("showProgress", true); }
    static boolean showBreaks(Context context) { return json(context).optBoolean("showBreaks", true); }
    static boolean showLunch(Context context) { return json(context).optBoolean("showLunch", true); }
    static boolean showWeekInfo(Context context) { return json(context).optBoolean("showWeekInfo", true); }
    static boolean colorByClass(Context context) { return json(context).optBoolean("colorByClass", false); }
    static String accessibility(Context context) { return json(context).optString("accessibility", "normal"); }
    static boolean remindersEnabled(Context context) { return json(context).optBoolean("remindersEnabled", false); }
    static int reminderMinutes(Context context) { return clamp(json(context).optInt("reminderMinutes", 10), 0, 120); }
    static String holidayMode(Context context) { return json(context).optString("holidayMode", "alsace_moselle"); }

    static String widgetGapLabel(Context context, String fallback) {
        String value = json(context).optString("gapWidgetLabel", "").trim();
        return value.isEmpty() ? (fallback == null ? "" : fallback) : value;
    }

    static String widgetLunchLabel(Context context, String fallback) {
        String value = json(context).optString("lunchWidgetLabel", "").trim();
        return value.isEmpty() ? (fallback == null ? "" : fallback) : value;
    }

    static String widgetCourseLabel(Context context, Calendar date, ScheduleData.Course course) {
        if (course == null) return "";
        String fallback = course.label == null ? "" : course.label;
        try {
            JSONObject map = json(context).optJSONObject("widgetCourseLabels");
            if (map == null) return fallback;
            String week = ScheduleStore.getWeekLetter(context, date);
            String key = week + "|" + date.get(Calendar.DAY_OF_WEEK) + "|" + course.start + "|" + course.end;
            String value = map.optString(key, "").trim();
            return value.isEmpty() ? fallback : value;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static boolean isDayOff(Context context, Calendar date) {
        int dow = date.get(Calendar.DAY_OF_WEEK);
        if ((dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) && !ScheduleStore.isDayEnabled(context, dow)) return true;
        String mode = holidayMode(context);
        if (!"off".equals(mode) && HolidayUtils.isFrenchHoliday(date, "alsace_moselle".equals(mode))) return true;
        String key = dateKey(date);
        JSONArray ranges = json(context).optJSONArray("dayOffRanges");
        if (ranges != null) {
            for (int i = 0; i < ranges.length(); i++) {
                JSONObject r = ranges.optJSONObject(i);
                if (r == null) continue;
                String start = r.optString("start", "");
                String end = r.optString("end", start);
                if (!start.isEmpty() && key.compareTo(start) >= 0 && key.compareTo(end) <= 0) return true;
            }
        }
        return false;
    }

    static List<ScheduleData.Course> applyExceptions(Context context, Calendar date, List<ScheduleData.Course> base) {
        List<ScheduleData.Course> out = new ArrayList<>();
        if (base != null) out.addAll(base);
        String key = dateKey(date);
        JSONArray arr = json(context).optJSONArray("exceptions");
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject e = arr.optJSONObject(i);
            if (e == null || !key.equals(e.optString("date", ""))) continue;
            String type = e.optString("type", "");
            String refStart = e.optString("refStart", "");
            String refLabel = e.optString("refLabel", "");
            if ("extra".equals(type)) {
                out.add(new ScheduleData.Course(e.optString("start", "08:00"), e.optString("end", "09:00"), e.optString("label", "Cours exceptionnel"), e.optString("room", ""), 0, false));
                continue;
            }
            int index = findMatching(out, refStart, refLabel);
            if (index < 0) continue;
            ScheduleData.Course old = out.get(index);
            if ("cancel".equals(type)) out.remove(index);
            else if ("room".equals(type)) out.set(index, new ScheduleData.Course(old.start, old.end, old.label, e.optString("room", old.room), old.slot, old.uncertain, old.color, old.badge));
            else if ("move".equals(type)) out.set(index, new ScheduleData.Course(e.optString("start", old.start), e.optString("end", old.end), e.optString("label", old.label), e.optString("room", old.room), 0, old.uncertain, old.color, old.badge));
        }
        Collections.sort(out, Comparator.comparingInt(c -> ScheduleData.toMinutes(c.start)));
        return out;
    }

    static int classColor(Context context, String label, int fallback) {
        if (!colorByClass(context) || label == null || label.trim().isEmpty()) return fallback;
        int[] standard = {0xFF0877F9, 0xFF00897B, 0xFF6750A4, 0xFF2E7D32, 0xFFEF6C00, 0xFFC2185B};
        int[] cb = {0xFF0072B2, 0xFFE69F00, 0xFF009E73, 0xFFCC79A7, 0xFFD55E00, 0xFF56B4E9};
        int[] palette = "colorblind".equals(accessibility(context)) ? cb : standard;
        int hash = Math.abs(label.toLowerCase(Locale.ROOT).hashCode());
        return palette[hash % palette.length];
    }

    static String dateKey(Calendar date) { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime()); }

    private static int findMatching(List<ScheduleData.Course> list, String start, String label) {
        for (int i = 0; i < list.size(); i++) {
            ScheduleData.Course c = list.get(i);
            if (!start.isEmpty() && !start.equals(c.start)) continue;
            if (!label.isEmpty() && !label.equalsIgnoreCase(c.label)) continue;
            return i;
        }
        return -1;
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}
