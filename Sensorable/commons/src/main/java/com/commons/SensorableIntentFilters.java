package com.commons;

import android.content.IntentFilter;

public class SensorableIntentFilters {
    public static final IntentFilter EMPATICA_SENSORS = new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA);
    public static final IntentFilter MOBILE_SENSORS = new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS);
    public static final IntentFilter WEAR_SENSORS = new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA);
}
