package com.sensorable.utils;

import com.commons.SensorableConstants;

import java.util.Calendar;

public class SensorableDates {
    public static String timestampToDate(long timestamp) {
        Calendar adlCalendar = Calendar.getInstance();
        adlCalendar.setTimeInMillis(timestamp);
        return adlCalendar.get(Calendar.DAY_OF_MONTH) +
                SensorableConstants.DATE_SEPARATOR +
                adlCalendar.get(Calendar.MONTH) + 1 +
                SensorableConstants.DATE_SEPARATOR +
                adlCalendar.get(Calendar.YEAR) +
                " " +
                adlCalendar.get(Calendar.HOUR_OF_DAY) +
                SensorableConstants.TIME_SEPARATOR +
                adlCalendar.get(Calendar.MINUTE) +
                SensorableConstants.TIME_SEPARATOR +
                adlCalendar.get(Calendar.SECOND);
    }
}
