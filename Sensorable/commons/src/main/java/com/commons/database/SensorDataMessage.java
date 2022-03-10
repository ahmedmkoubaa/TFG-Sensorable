package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(primaryKeys = {"device_type", "sensor_type", "values", "timestamp"})
public class SensorDataMessage {
    @NonNull
    @ColumnInfo(name = "device_type")
    public int deviceType;

    @NonNull
    @ColumnInfo(name = "sensor_type")
    public int sensorType;

    @NonNull
    @ColumnInfo(name = "values")
    public String values;

    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;
}
