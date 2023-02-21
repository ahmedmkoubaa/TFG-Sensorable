package com.sensorable.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.commons.utils.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.BluetoothDeviceRegistryDao;
import com.commons.database.BluetoothDeviceRegistryEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.commons.database.SensorableDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothDetectionService extends Service {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BluetoothDeviceDao bluetoothDeviceDao;
    private BluetoothDeviceRegistryDao bluetoothDeviceRegistryDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeMobileDatabase();
        initializeBluetoothProvider();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMobileDatabase() {
        SensorableDatabase database = MobileDatabaseBuilder.getDatabase(this);

        bluetoothDeviceDao = database.bluetoothDeviceDao();
        bluetoothDeviceRegistryDao = database.bluetoothDeviceRegistryDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeBluetoothProvider() {
        // Register of local receiver to listen to found bluetooth devices
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("BLUETOOTH_DETECTION_SERVICE", "receiving from bluetooth");

                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    bluetoothDeviceFound(intent);
                }
            }
        }, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        scheduler.scheduleAtFixedRate(() -> {
            Log.i("BLUETOOTH_DETECTION_SERVICE", "did discovery");
            BluetoothDevicesProvider.startDiscovery();

        }, 0, SensorableConstants.SCHEDULE_BLUETOOTH_DISCOVERY, TimeUnit.MILLISECONDS);
    }

    private void bluetoothDeviceFound(Intent intent) {

        // Discovery has found a device. Get the BluetoothDevice object and its info from the Intent.
        BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // update the registry of detected devices
        updateRegistry(foundDevice);
    }

    private void updateRegistry(BluetoothDevice foundDevice) {
        executor.execute(() -> {

            BluetoothDeviceEntity searched = bluetoothDeviceDao.findByAddress(foundDevice.getAddress());
            if (searched == null) {
                // insert a new detected device into the database
                bluetoothDeviceDao.insert(new BluetoothDeviceEntity(foundDevice));
                Log.i("BLUETOOTH_DETECTION_SERVICE", "added a new bluetooth device");
            } else {
                Log.i("BLUETOOTH_DETECTION_SERVICE", "bluetooth device was previously added");
            }

            long currentTimestamp = new Date().getTime();

            BluetoothDeviceRegistryEntity foundRegistry =
                    bluetoothDeviceRegistryDao.getDevicesInRange(
                            currentTimestamp - SensorableConstants.TIME_SINCE_LAST_BLUETOOTH_DETECTION, currentTimestamp);


            if (foundRegistry == null) {
                // register a new detected device into the local database registry
                bluetoothDeviceRegistryDao.insert(
                        new BluetoothDeviceRegistryEntity(foundDevice.getAddress(), currentTimestamp)
                );

                Log.i("BLUETOOTH_DETECTION_SERVICE", "added a new bluetooth device registry");

            } else {
                foundRegistry.end = currentTimestamp;
                bluetoothDeviceRegistryDao.updateDevice(foundRegistry);
                Log.i(
                        "BLUETOOTH_DETECTION_SERVICE",
                        "inserting new with elapsed time " + ((currentTimestamp - SensorableConstants.TIME_SINCE_LAST_BLUETOOTH_DETECTION) / 1000) + " seconds"
                );

            }
        });
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}