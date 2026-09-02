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
        Course(String start, String end, String label, String room) {
            this.start = start; this.end = end; this.label = label; this.room = room;
        }
    }

    private static final Map<Integer, List<Course>> DEFAULTS = new HashMap<>();
    static {
        DEFAULTS.put(Calendar.MONDAY, Arrays.asList(c("08:00","09:00","4G1 ALL · 4G2 ALL","217"),c("10:00","11:00","6G3BIL · 6G4BIL","217"),c("11:00","12:00","4G3 ALL · 4G4 ALL","217"),c("13:00","14:00","5G1-2-3 ALL","216")));
        DEFAULTS.put(Calendar.TUESDAY, Arrays.asList(c("08:00","09:00","5G4 ALL","217"),c("09:00","10:00","4G3 ALL · 4G4 ALL","217"),c("14:00","15:00","3G3 ALL · 3G4 ALL","216"),c("16:00","17:00","3G1 ALL · 3G2 ALL","216")));
        DEFAULTS.put(Calendar.WEDNESDAY, Collections.emptyList());
        DEFAULTS.put(Calendar.THURSDAY, Arrays.asList(c("08:00","09:00","3G1 ALL · 3G2 ALL","216"),c("09:00","10:00","3G3 ALL · 3G4 ALL","216"),c("10:00","11:00","6G3BIL · 6G4BIL","216"),c("11:00","12:00","5G4 ALL","216"),c("13:00","14:00","6G3BIL · 6G4BIL","216"),c("14:00","15:00","4G1 ALL · 4G2 ALL","215"),c("16:00","17:00","5G1-2-3 ALL","217")));
        DEFAULTS.put(Calendar.FRIDAY, Arrays.asList(c("11:00","12:00","5G1-2-3 ALL","217"),c("13:00","14:00","5G4 ALL","217"),c("14:00","15:00","3G3 ALL · 3G4 ALL","217"),c("15:00","16:00","4G3 ALL · 4G4 ALL","217"),c("16:00","17:00","4G1 ALL · 4G2 ALL","217")));
    }
    private static Course c(String s,String e,String l,String r){ return new Course(s,e,l,r); }
    static List<Course> defaultForDay(int day){ List<Course> l=DEFAULTS.get(day); return l==null?Collections.emptyList():new ArrayList<>(l); }
    static int toMinutes(String hhmm){ try{String[] p=hhmm.split(":");return Integer.parseInt(p[0])*60+Integer.parseInt(p[1]);}catch(Exception e){return 0;} }
    static List<Integer> boundaries(List<Course> courses){ List<Integer> r=new ArrayList<>();for(Course c:courses){r.add(toMinutes(c.start));r.add(toMinutes(c.end));}Collections.sort(r);return r; }
}
