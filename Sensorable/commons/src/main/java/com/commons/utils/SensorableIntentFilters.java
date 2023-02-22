package com.commons.utils;

import android.content.IntentFilter;

public class SensorableIntentFilters {
    public static final IntentFilter EMPATICA_SENSORS = new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA);
    public static final IntentFilter SERVICE_PROVIDER_SENSORS = new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS);
    public static final IntentFilter WEAR_SENSORS = new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA);
    public static final IntentFilter SENSORS_PROVIDER_LOCATION = new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_LOCATION);
}
