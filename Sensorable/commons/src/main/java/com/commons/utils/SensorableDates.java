package com.commons.utils;

import java.util.Calendar;

public class SensorableDates {
    private final static Calendar adlCalendar = Calendar.getInstance();

    public static String timestampToDate(long timestamp) {
        adlCalendar.setTimeInMillis(timestamp);
        return adlCalendar.get(Calendar.DAY_OF_MONTH) +
                SensorableConstants.DATE_SEPARATOR +
                (adlCalendar.get(Calendar.MONTH) + 1) +
                SensorableConstants.DATE_SEPARATOR +
                adlCalendar.get(Calendar.YEAR) +
                " " +
                timestampToTimeSeconds(timestamp);
    }

    public static String timestampToTimeSeconds(long timestamp) {
        adlCalendar.setTimeInMillis(timestamp);
        return adlCalendar.get(Calendar.HOUR_OF_DAY) +
                SensorableConstants.TIME_SEPARATOR +
                adlCalendar.get(Calendar.MINUTE) +
                SensorableConstants.TIME_SEPARATOR +
                adlCalendar.get(Calendar.SECOND);
    }
}
