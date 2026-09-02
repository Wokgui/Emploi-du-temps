package com.wokgui.schedulewidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ScheduleData {
    static final class Course {
        final String start, end, label, room;
        final int slot;

        Course(String start, String end, String label, String room) {
            this(start, end, label, room, 0);
        }

        Course(String start, String end, String label, String room, int slot) {
            this.start = start;
            this.end = end;
            this.label = label;
            this.room = room;
            this.slot = slot;
        }
    }

    private static final Map<Integer, List<Course>> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(Calendar.MONDAY, Arrays.asList(
                c("08:00","09:00","4G1 ALL · 4G2 ALL","217",1),
                c("10:00","11:00","6G3BIL · 6G4BIL","217",3),
                c("11:00","12:00","4G3 ALL · 4G4 ALL","217",4),
                c("13:00","14:00","5G1-2-3 ALL","216",5)
        ));
        DEFAULTS.put(Calendar.TUESDAY, Arrays.asList(
                c("08:00","09:00","5G4 ALL","217",1),
                c("09:00","10:00","4G3 ALL · 4G4 ALL","217",2),
                c("14:00","15:00","3G3 ALL · 3G4 ALL","216",6),
                c("16:00","17:00","3G1 ALL · 3G2 ALL","216",7)
        ));
        DEFAULTS.put(Calendar.WEDNESDAY, Collections.emptyList());
        DEFAULTS.put(Calendar.THURSDAY, Arrays.asList(
                c("08:00","09:00","3G1 ALL · 3G2 ALL","216",1),
                c("09:00","10:00","3G3 ALL · 3G4 ALL","216",2),
                c("10:00","11:00","6G3BIL · 6G4BIL","216",3),
                c("11:00","12:00","5G4 ALL","216",4),
                c("13:00","14:00","6G3BIL · 6G4BIL","216",5),
                c("14:00","15:00","4G1 ALL · 4G2 ALL","215",6),
                c("16:00","17:00","5G1-2-3 ALL","217",7)
        ));
        DEFAULTS.put(Calendar.FRIDAY, Arrays.asList(
                c("11:00","12:00","5G1-2-3 ALL","217",4),
                c("13:00","14:00","5G4 ALL","217",5),
                c("14:00","15:00","3G3 ALL · 3G4 ALL","217",6),
                c("15:00","16:00","4G3 ALL · 4G4 ALL","217",0),
                c("16:00","17:00","4G1 ALL · 4G2 ALL","217",7)
        ));
    }

    private static Course c(String s, String e, String l, String r, int slot) {
        return new Course(s, e, l, r, slot);
    }

    static List<Course> defaultForDay(int day) {
        List<Course> list = DEFAULTS.get(day);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    static int toMinutes(String hhmm) {
        try {
            String[] p = hhmm.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    static List<Integer> boundaries(List<Course> courses) {
        List<Integer> result = new ArrayList<>();
        for (Course c : courses) {
            result.add(toMinutes(c.start));
            result.add(toMinutes(c.end));
        }
        Collections.sort(result);
        return result;
    }
}
