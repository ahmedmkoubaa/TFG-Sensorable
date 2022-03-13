package com.sensorable.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.KnownLocationDao;
import com.commons.database.KnownLocationEntity;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;

@Database(entities = {
        BluetoothDeviceEntity.class,
        SensorMessageEntity.class,
        KnownLocationEntity.class
}, version = SensorableConstants.MOBILE_DATABASE_VERSION)
public abstract class MobileDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();

    public abstract SensorMessageDao sensorMessageDao();

    public abstract KnownLocationDao knownLocationDao();
}






