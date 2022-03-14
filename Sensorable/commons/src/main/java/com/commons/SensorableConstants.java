package com.commons;

public class SensorableConstants {
    public final static String MOBILE_DATABASE_NAME = "default-mobile-database";
    public final static int MOBILE_DATABASE_NUMBER_THREADS = 1;
    public final static int MOBILE_DATABASE_VERSION = 18;

    // time that uses the adl detection to service to filter data
    // the lower is this value more accurate is the filtering, it means
    // it filter less info and process a bigger amount of data
    public final static int ADL_FILTER_TIME = 2000; // in milliseconds
    public final static long TIME_SINCE_LAST_ADL_DETECTION = 5 * 60 * 1000;

    public final static long TIME_SINCE_LAST_BLUETOOTH_DETECTION = 30 * 60 * 1000; // in milliseconds
    public final static int SCHEDULE_BLUETOOTH_DISCOVERY = 5 * 1000; // in milliseconds
}
