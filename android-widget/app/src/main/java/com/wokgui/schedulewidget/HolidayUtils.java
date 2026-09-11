package com.wokgui.schedulewidget;

import java.util.Calendar;

final class HolidayUtils {
    private HolidayUtils() {}

    static boolean isFrenchHoliday(Calendar date, boolean alsaceMoselle) {
        int y = date.get(Calendar.YEAR);
        int m = date.get(Calendar.MONTH) + 1;
        int d = date.get(Calendar.DAY_OF_MONTH);
        if ((m == 1 && d == 1) || (m == 5 && d == 1) || (m == 5 && d == 8)
                || (m == 7 && d == 14) || (m == 8 && d == 15) || (m == 11 && d == 1)
                || (m == 11 && d == 11) || (m == 12 && d == 25)) return true;
        if (alsaceMoselle && m == 12 && d == 26) return true;

        Calendar easter = easterSunday(y);
        if (sameDay(date, plusDays(easter, 1))) return true;   // lundi de Pâques
        if (sameDay(date, plusDays(easter, 39))) return true;  // Ascension
        if (sameDay(date, plusDays(easter, 50))) return true;  // lundi de Pentecôte
        return alsaceMoselle && sameDay(date, plusDays(easter, -2)); // vendredi saint
    }

    private static Calendar easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int mm = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * mm + 114) / 31;
        int day = ((h + l - 7 * mm + 114) % 31) + 1;
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, 12, 0, 0);
        return cal;
    }

    private static Calendar plusDays(Calendar source, int days) {
        Calendar c = (Calendar) source.clone();
        c.add(Calendar.DAY_OF_YEAR, days);
        return c;
    }

    private static boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
