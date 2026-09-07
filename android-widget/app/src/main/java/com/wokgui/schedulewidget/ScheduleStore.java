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
    private static final String WEEK_AB_INIT = "week_ab_initialized";
    private static final String WEEK_A_PARITY = "week_a_parity";
    private static final String CYCLE_ANCHOR = "cycle_anchor_week_index";
    private static final String GAP_LABEL = "gap_label";
    private static final String LUNCH_LABEL = "lunch_label";
    private static final String SHOW_GAP_BADGE = "show_gap_badge";
    private static final String SHOW_LUNCH_BADGE = "show_lunch_badge";
    private static final String[] LETTERS = {"A", "B", "C", "D"};

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
            if (!p.contains("slot_" + (i + 1) + "_start")) e.putString("slot_" + (i + 1) + "_start", DEFAULT_START[i]);
            if (!p.contains("slot_" + (i + 1) + "_end")) e.putString("slot_" + (i + 1) + "_end", DEFAULT_END[i]);
        }
        if (!p.contains(GAP_LABEL)) e.putString(GAP_LABEL, "Trou");
        if (!p.contains(LUNCH_LABEL)) e.putString(LUNCH_LABEL, "Midi");
        if (!p.contains(SHOW_GAP_BADGE)) e.putBoolean(SHOW_GAP_BADGE, false);
        if (!p.contains(SHOW_LUNCH_BADGE)) e.putBoolean(SHOW_LUNCH_BADGE, false);
        for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) e.putBoolean("enabled_" + day, true);
        e.apply();

        p = prefs(context);
        if (!p.getBoolean(WEEK_AB_INIT, false)) {
            SharedPreferences.Editor migration = p.edit();
            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                String legacy = p.getString("day_" + day, encode(ScheduleData.defaultForDay(day)));
                migration.putString(weekKey("A", day), legacy);
                migration.putString(weekKey("B", day), legacy);
            }
            migration.putInt(WEEK_A_PARITY, Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) & 1);
            migration.putBoolean(WEEK_AB_INIT, true);
            migration.apply();
        }

        p = prefs(context);
        SharedPreferences.Editor cycle = p.edit();
        for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
            String a = p.getString(weekKey("A", day), encode(ScheduleData.defaultForDay(day)));
            String b = p.getString(weekKey("B", day), a);
            if (!p.contains(weekKey("C", day))) cycle.putString(weekKey("C", day), a);
            if (!p.contains(weekKey("D", day))) cycle.putString(weekKey("D", day), b);
        }
        if (!p.contains(CYCLE_ANCHOR)) {
            Calendar now = Calendar.getInstance();
            int oldParity = p.getInt(WEEK_A_PARITY, now.get(Calendar.WEEK_OF_YEAR) & 1);
            boolean isA = (now.get(Calendar.WEEK_OF_YEAR) & 1) == oldParity;
            cycle.putInt(CYCLE_ANCHOR, weekIndex(now) - (isA ? 0 : 1));
        }
        cycle.apply();
    }

    static boolean isDayEnabled(Context context, int day) { ensureInitialized(context); return true; }

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

    static String getGapLabel(Context context) {
        ensureInitialized(context);
        String value = prefs(context).getString(GAP_LABEL, "Trou");
        return value == null ? "Trou" : value.trim();
    }

    static String getLunchLabel(Context context) {
        ensureInitialized(context);
        String value = prefs(context).getString(LUNCH_LABEL, "Midi");
        return value == null ? "Midi" : value.trim();
    }

    static boolean showGapBadge(Context context) { ensureInitialized(context); return prefs(context).getBoolean(SHOW_GAP_BADGE, false); }
    static boolean showLunchBadge(Context context) { ensureInitialized(context); return prefs(context).getBoolean(SHOW_LUNCH_BADGE, false); }

    static String getWeekLetter(Context context, Calendar date) {
        ensureInitialized(context);
        int length = AdvancedSettingsStore.cycleLength(context);
        int anchor = prefs(context).getInt(CYCLE_ANCHOR, weekIndex(Calendar.getInstance()));
        int index = Math.floorMod(weekIndex(date) - anchor, length);
        return LETTERS[index];
    }

    static void setCurrentWeekLetter(Context context, String letter) {
        ensureInitialized(context);
        int length = AdvancedSettingsStore.cycleLength(context);
        int desired = letterIndex(letter);
        if (desired < 0 || desired >= length) desired = 0;
        int anchor = weekIndex(Calendar.getInstance()) - desired;
        prefs(context).edit().putInt(CYCLE_ANCHOR, anchor).apply();
        refreshWidgets(context);
    }

    static List<ScheduleData.Course> getStoredCourses(Context context, int day, String week) {
        ensureInitialized(context);
        String safeWeek = safeWeek(week);
        String json = prefs(context).getString(weekKey(safeWeek, day), "[]");
        List<ScheduleData.Course> result = decode(json);
        Collections.sort(result, Comparator.comparingInt(c -> ScheduleData.toMinutes(c.start)));
        return result;
    }

    static List<ScheduleData.Course> getStoredCourses(Context context, int day) {
        return getStoredCourses(context, day, getWeekLetter(context, Calendar.getInstance()));
    }

    static List<ScheduleData.Course> getCourses(Context context, Calendar date) {
        if (AdvancedSettingsStore.isDayOff(context, date)) return new ArrayList<>();
        List<ScheduleData.Course> base = getStoredCourses(context, date.get(Calendar.DAY_OF_WEEK), getWeekLetter(context, date));
        return AdvancedSettingsStore.applyExceptions(context, date, base);
    }

    static List<ScheduleData.Course> getCourses(Context context, int day) {
        return getStoredCourses(context, day, getWeekLetter(context, Calendar.getInstance()));
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

            JSONObject breaks = new JSONObject();
            breaks.put("gapLabel", getGapLabel(context));
            breaks.put("lunchLabel", getLunchLabel(context));
            breaks.put("showGapBadge", showGapBadge(context));
            breaks.put("showLunchBadge", showLunchBadge(context));
            root.put("_breaks", breaks);

            Calendar now = Calendar.getInstance();
            String currentWeek = getWeekLetter(context, now);
            root.put("_currentWeek", currentWeek);
            root.put("_weekAnchor", prefs(context).getInt(CYCLE_ANCHOR, weekIndex(now)));
            root.put("_cycleLength", AdvancedSettingsStore.cycleLength(context));

            JSONObject weeks = new JSONObject();
            for (String week : LETTERS) {
                JSONObject weekObject = new JSONObject();
                for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                    JSONObject d = new JSONObject();
                    d.put("enabled", true);
                    d.put("courses", new JSONArray(encode(getStoredCourses(context, day, week))));
                    weekObject.put(String.valueOf(day), d);
                }
                weeks.put(week, weekObject);
            }
            root.put("_weeks", weeks);

            for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                JSONObject d = new JSONObject();
                d.put("enabled", true);
                d.put("courses", new JSONArray(encode(getStoredCourses(context, day, currentWeek))));
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
                    editor.putString("slot_" + (i + 1) + "_start", s.optString("start", DEFAULT_START[i]));
                    editor.putString("slot_" + (i + 1) + "_end", s.optString("end", DEFAULT_END[i]));
                }
            }

            JSONObject breaks = root.optJSONObject("_breaks");
            if (breaks != null) {
                editor.putString(GAP_LABEL, breaks.optString("gapLabel", "Trou"));
                editor.putString(LUNCH_LABEL, breaks.optString("lunchLabel", "Midi"));
                editor.putBoolean(SHOW_GAP_BADGE, false);
                editor.putBoolean(SHOW_LUNCH_BADGE, false);
            }

            Calendar now = Calendar.getInstance();
            if (root.has("_weekAnchor")) {
                editor.putInt(CYCLE_ANCHOR, root.optInt("_weekAnchor", weekIndex(now)));
            } else {
                String requestedCurrent = root.optString("_currentWeek", "");
                int idx = letterIndex(requestedCurrent);
                if (idx >= 0) editor.putInt(CYCLE_ANCHOR, weekIndex(now) - idx);
            }

            JSONObject weeks = root.optJSONObject("_weeks");
            if (weeks != null) {
                for (String week : LETTERS) {
                    JSONObject weekObject = weeks.optJSONObject(week);
                    if (weekObject == null) continue;
                    for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                        JSONObject d = weekObject.optJSONObject(String.valueOf(day));
                        if (d == null) continue;
                        JSONArray arr = d.optJSONArray("courses");
                        if (arr != null) editor.putString(weekKey(week, day), arr.toString());
                    }
                }
            } else {
                for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; day++) {
                    JSONObject d = root.optJSONObject(String.valueOf(day));
                    if (d == null) continue;
                    JSONArray arr = d.optJSONArray("courses");
                    if (arr != null) {
                        for (String week : LETTERS) editor.putString(weekKey(week, day), arr.toString());
                    }
                }
            }

            editor.putBoolean(WEEK_AB_INIT, true);
            editor.apply();
            if (root.has("_cycleLength")) AdvancedSettingsStore.setCycleLength(context, root.optInt("_cycleLength", 2));
            refreshWidgets(context);
        } catch (Exception ignored) {}
    }

    static void refreshWidgets(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, ScheduleWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        manager.notifyAppWidgetViewDataChanged(ids, R.id.upcomingList);
        Intent refresh = new Intent(context, ScheduleWidgetProvider.class).setAction(ScheduleWidgetProvider.ACTION_REFRESH);
        context.sendBroadcast(refresh);
        ReminderScheduler.reschedule(context);
    }

    private static String weekKey(String week, int day) { return "week_" + week + "_day_" + day; }

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
                o.put("uncertain", c.uncertain);
                if (!c.color.isEmpty()) o.put("color", c.color);
                if (!c.badge.isEmpty()) o.put("badge", c.badge);
                arr.put(o);
            }
        } catch (Exception ignored) {}
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
                boolean uncertain = o.optBoolean("uncertain", false);
                String color = o.optString("color", "");
                String badge = o.optString("badge", "");
                if (slot == 0) {
                    for (int n = 0; n < 7; n++) {
                        if (DEFAULT_START[n].equals(start) && DEFAULT_END[n].equals(end)) { slot = n + 1; break; }
                    }
                }
                out.add(new ScheduleData.Course(start, end, label, room, slot, uncertain, color, badge));
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static String safeWeek(String week) {
        if ("B".equalsIgnoreCase(week)) return "B";
        if ("C".equalsIgnoreCase(week)) return "C";
        if ("D".equalsIgnoreCase(week)) return "D";
        return "A";
    }

    private static int letterIndex(String letter) {
        if ("B".equalsIgnoreCase(letter)) return 1;
        if ("C".equalsIgnoreCase(letter)) return 2;
        if ("D".equalsIgnoreCase(letter)) return 3;
        if ("A".equalsIgnoreCase(letter)) return 0;
        return -1;
    }

    private static int weekIndex(Calendar date) {
        Calendar c = (Calendar) date.clone();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        int dow = c.get(Calendar.DAY_OF_WEEK);
        int delta = dow == Calendar.SUNDAY ? -6 : Calendar.MONDAY - dow;
        c.add(Calendar.DAY_OF_YEAR, delta);
        return (int) Math.floorDiv(c.getTimeInMillis(), 7L * 24L * 60L * 60L * 1000L);
    }
}
