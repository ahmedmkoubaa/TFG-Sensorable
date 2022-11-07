package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"device_type", "sensor_type", "values_x", "values_y", "values_z", "timestamp"})
public class SensorMessageEntity {
    @NonNull
    @ColumnInfo(name = "device_type")
    public int deviceType;

    @NonNull
    @ColumnInfo(name = "sensor_type")
    public int sensorType;

    @NonNull
    @ColumnInfo(name = "values_x")
    public float valuesX;

    @ColumnInfo(name = "values_y")
    public float valuesY;

    @ColumnInfo(name = "values_z")
    public float valuesZ;

    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "user_id")
    public String userId;

    public SensorMessageEntity(@NonNull int deviceType, @NonNull int sensorType, @NonNull float valuesX, float valuesY, float valuesZ, @NonNull long timestamp, @NonNull String userId) {
        this.deviceType = deviceType;
        this.sensorType = sensorType;
        this.valuesX = valuesX;
        this.valuesY = valuesY;
        this.valuesZ = valuesZ;
        this.timestamp = timestamp;
        this.userId = userId;
    }


    public String toJson() {
        return "[" + deviceType + "," + sensorType + "," + valuesX + "," + valuesY + "," + valuesZ + "," + timestamp + ",\"" + userId + "\" ]";
    }

}
