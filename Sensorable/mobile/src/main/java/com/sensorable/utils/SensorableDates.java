package com.sensorable.utils;

import java.util.Calendar;

public class SensorableDates {
    public static String timestampToDate(long timestamp) {
        Calendar adlCalendar = Calendar.getInstance();
        adlCalendar.setTimeInMillis(timestamp);
        return adlCalendar.get(Calendar.DAY_OF_MONTH) +
                "/" +
                adlCalendar.get(Calendar.MONTH) +
                "/" +
                adlCalendar.get(Calendar.YEAR) +
                " " +
                adlCalendar.get(Calendar.HOUR_OF_DAY) +
                ":" +
                adlCalendar.get(Calendar.MINUTE) +
                ":" +
                adlCalendar.get(Calendar.SECOND);
    }
}
