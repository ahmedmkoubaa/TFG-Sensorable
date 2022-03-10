package com.sensorable.utils;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.commons.SensorableConstants;
import com.commons.database.BluetoothDevice;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.SensorDataMessage;
import com.commons.database.SensorDataMessageDao;

@Database(entities = {BluetoothDevice.class, SensorDataMessage.class}, version = SensorableConstants.MOBILE_DATABASE_VERSION)
public abstract class MobileDatabase extends RoomDatabase {
    public abstract BluetoothDeviceDao bluetoothDeviceDao();
    public abstract SensorDataMessageDao sensorMessageDao();
}
