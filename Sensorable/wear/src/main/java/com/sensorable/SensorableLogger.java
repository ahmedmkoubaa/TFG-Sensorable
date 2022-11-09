package com.sensorable;

import com.commons.SensorableConstants;
import com.commons.SensorableDates;

import java.util.ArrayList;
import java.util.Date;

public class SensorableLogger {
    private static final ArrayList<String> loggedData = new ArrayList<String>();
    public static LoggerAdapter adapter;

    // log with timestamp included automatically
    public static void log(String msg) {
        loggedData.add(msg + " - " + getTimeString());
        notifyDataChanged();
        removeExtraData();
    }

    private static void removeExtraData() {
        if (loggedData.size() > SensorableConstants.MAX_WEAR_OS_LOGGER_ELEMENTS) {
            loggedData.remove(0);
        }
    }

    private static void notifyDataChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static final String getTimeString() {
        return SensorableDates.timestampToTimeSeconds(getTime());
    }

    private static final long getTime() {
        return new Date().getTime();
    }

    public static ArrayList<String> getLoggedData() {
        return loggedData;
    }

    public static void setAdapter(LoggerAdapter adapter) {
        SensorableLogger.adapter = adapter;
    }
}

