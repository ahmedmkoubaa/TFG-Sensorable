package com.sensorable;

import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.example.commons.SensorableConstants;
import com.example.commons.database.BluetoothDevice;
import com.example.commons.database.BluetoothDeviceDao;

public class LocalDatabaseService extends Service {
    private BluetoothDeviceDao bluetoothDeviceDao;

    public LocalDatabaseService() {
        initializeDatabase();
    }

    public LocalDatabaseService(BluetoothDeviceDao bluetoothDeviceDao) {
        this.bluetoothDeviceDao = bluetoothDeviceDao;
    }

    private void initializeDatabase() {

        MobileDatabase database = Room.databaseBuilder(
               this,
                MobileDatabase.class,
                SensorableConstants.MOBILE_DATABASE_NAME)
                .build();

        bluetoothDeviceDao = database.bluetoothDeviceDao();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "DATABASE SERVICE", Toast.LENGTH_SHORT).show();

        BluetoothDevice b = new BluetoothDevice();
        b.deviceName = "ADDED FROM SERVICE";
        b.bondState = 0;
        b.address = "00:00:00:00:00:00";
        b.bluetoothDeviceType = BluetoothClass.Device.PHONE_SMART;
        b.trusted = false;

        bluetoothDeviceDao.insert(b);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}