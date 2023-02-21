package com.commons.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.commons.database.ActivityDao;
import com.commons.database.ActivityEntity;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.AdlRegistryDao;
import com.commons.database.AdlRegistryEntity;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.BluetoothDeviceRegistryDao;
import com.commons.database.BluetoothDeviceRegistryEntity;
import com.commons.database.EventDao;
import com.commons.database.EventEntity;
import com.commons.database.EventForAdlDao;
import com.commons.database.EventForAdlEntity;
import com.commons.database.KnownLocationDao;
import com.commons.database.KnownLocationEntity;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.commons.database.StepsForActivitiesDao;
import com.commons.database.StepsForActivitiesEntity;
import com.commons.database.StepsForActivitiesRegistryDao;
import com.commons.database.StepsForActivitiesRegistryEntity;

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
        StepsForActivitiesRegistryEntity.class
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






