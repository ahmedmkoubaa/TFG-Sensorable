package com.sensorable.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.commons.SensorableConstants;
import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.AdlRegistryDao;
import com.commons.database.AdlRegistryEntity;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.DetectedAdlDao;
import com.commons.database.DetectedAdlEntity;
import com.commons.database.EventDao;
import com.commons.database.EventEntity;
import com.commons.database.EventForAdlDao;
import com.commons.database.EventForAdlEntity;
import com.commons.database.KnownLocationDao;
import com.commons.database.KnownLocationEntity;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;

@Database(entities = {
        BluetoothDeviceEntity.class,
        SensorMessageEntity.class,
        KnownLocationEntity.class,
        DetectedAdlEntity.class,
        AdlEntity.class,
        EventEntity.class,
        EventForAdlEntity.class,
        AdlRegistryEntity.class
}, version = SensorableConstants.MOBILE_DATABASE_VERSION)

public abstract class MobileDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();

    public abstract SensorMessageDao sensorMessageDao();

    public abstract KnownLocationDao knownLocationDao();

    public abstract DetectedAdlDao detectedAdlDao();

    public abstract AdlDao adlDao();

    public abstract EventDao eventDao();

    public abstract EventForAdlDao eventForAdlDao();

    public abstract AdlRegistryDao adlRegistryDao();
}






