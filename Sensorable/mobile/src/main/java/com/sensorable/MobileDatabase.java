package com.sensorable;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.commons.database.BluetoothDevice;
import com.example.commons.database.BluetoothDeviceDao;

@Database(entities = {BluetoothDevice.class}, version = 1)
public abstract class MobileDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();
}
