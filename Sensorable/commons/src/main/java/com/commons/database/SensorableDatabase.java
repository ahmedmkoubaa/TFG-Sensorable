package com.commons.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.commons.utils.SensorableConstants;

@Database(entities = {
        BluetoothDeviceEntity.class,
        SensorMessageEntity.class,
        KnownLocationEntity.class,
        AdlEntity.class,
        EventEntity.class,
        EventForAdlEntity.class,
        AdlRegistryEntity.class,
        BluetoothDeviceRegistryEntity.class,
        ActivityEntity.class,
        ActivityStepEntity.class,
        StepsForActivitiesEntity.class,
        StepsForActivitiesRegistryEntity    .class
}, version = SensorableConstants.MOBILE_DATABASE_VERSION)

public abstract class SensorableDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();

    public abstract SensorMessageDao sensorMessageDao();

    public abstract KnownLocationDao knownLocationDao();

    public abstract AdlDao adlDao();

    public abstract EventDao eventDao();

    public abstract EventForAdlDao eventForAdlDao();

    public abstract AdlRegistryDao adlRegistryDao();

    public abstract BluetoothDeviceRegistryDao bluetoothDeviceRegistryDao();

    public abstract ActivityDao activityDao();

    public abstract ActivityStepDao activityStepDao();

    public abstract StepsForActivitiesDao stepsForActivitiesDao();

    public abstract StepsForActivitiesRegistryDao stepsForActivitiesRegistryDao();
}






