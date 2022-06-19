package com.sensorable;

import com.commons.SensorableDates;

import java.util.ArrayList;
import java.util.Date;

public class SensorableLogger {
    private static final ArrayList<String> loggedData = new ArrayList<String>();

    // log with timestamp included automatically
    public static void log(String msg) {
        loggedData.add(0, msg + " - " + SensorableDates.timestampToTimeSeconds(new Date().getTime()));
    }

    public static ArrayList<String> get() {
        return loggedData;
    }
}

