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
        final String start;
        final String end;
        final String label;
        final String room;

        Course(String start, String end, String label, String room) {
            this.start = start;
            this.end = end;
            this.label = label;
            this.room = room;
        }
    }

    private static final Map<Integer, List<Course>> SCHEDULE = new HashMap<>();

    static {
        SCHEDULE.put(Calendar.MONDAY, Arrays.asList(
                c("08:00", "09:00", "4G1 ALL · 4G2 ALL", "217"),
                c("10:00", "11:00", "6G3BIL · 6G4BIL", "217"),
                c("11:00", "12:00", "4G3 ALL · 4G4 ALL", "217"),
                c("13:00", "14:00", "5G1-2-3 ALL", "216")
        ));
        SCHEDULE.put(Calendar.TUESDAY, Arrays.asList(
                c("08:00", "09:00", "5G4 ALL", "217"),
                c("09:00", "10:00", "4G3 ALL · 4G4 ALL", "217"),
                c("14:00", "15:00", "3G3 ALL · 3G4 ALL", "216"),
                c("16:00", "17:00", "3G1 ALL · 3G2 ALL", "216")
        ));
        SCHEDULE.put(Calendar.WEDNESDAY, Collections.emptyList());
        SCHEDULE.put(Calendar.THURSDAY, Arrays.asList(
                c("08:00", "09:00", "3G1 ALL · 3G2 ALL", "216"),
                c("09:00", "10:00", "3G3 ALL · 3G4 ALL", "216"),
                c("10:00", "11:00", "6G3BIL · 6G4BIL", "216"),
                c("11:00", "12:00", "5G4 ALL", "216"),
                c("13:00", "14:00", "6G3BIL · 6G4BIL", "216"),
                c("14:00", "15:00", "4G1 ALL · 4G2 ALL", "215"),
                c("16:00", "17:00", "5G1-2-3 ALL", "217")
        ));
        SCHEDULE.put(Calendar.FRIDAY, Arrays.asList(
                c("11:00", "12:00", "5G1-2-3 ALL", "217"),
                c("13:00", "14:00", "5G4 ALL", "217"),
                c("14:00", "15:00", "3G3 ALL · 3G4 ALL", "217"),
                c("15:00", "16:00", "4G3 ALL · 4G4 ALL", "217"),
                c("16:00", "17:00", "4G1 ALL · 4G2 ALL", "217")
        ));
    }

    private ScheduleData() {}

    private static Course c(String start, String end, String label, String room) {
        return new Course(start, end, label, room);
    }

    static List<Course> forDay(int calendarDay) {
        List<Course> courses = SCHEDULE.get(calendarDay);
        return courses == null ? Collections.emptyList() : courses;
    }

    static int toMinutes(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    static List<Integer> allBoundariesForDay(int calendarDay) {
        List<Integer> result = new ArrayList<>();
        for (Course course : forDay(calendarDay)) {
            result.add(toMinutes(course.start));
            result.add(toMinutes(course.end));
        }
        Collections.sort(result);
        return result;
    }
}
