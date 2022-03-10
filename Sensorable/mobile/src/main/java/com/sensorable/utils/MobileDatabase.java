package com.sensorable.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.commons.database.BluetoothDevice;
import com.commons.database.BluetoothDeviceDao;

@Database(entities = {BluetoothDevice.class}, version = 1)
public abstract class MobileDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();
}
