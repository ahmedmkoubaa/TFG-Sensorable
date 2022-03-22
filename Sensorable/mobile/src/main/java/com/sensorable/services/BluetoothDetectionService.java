package com.sensorable.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothDetectionService extends Service {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private BluetoothDeviceDao bluetoothDeviceDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "BLUETOOTH DETECTION SERVICE", Toast.LENGTH_SHORT).show();
        initializeMobileDatabase();
        initializeBluetoothProvider();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMobileDatabase() {
        bluetoothDeviceDao = MobileDatabaseBuilder.getDatabase(this).bluetoothDeviceDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeBluetoothProvider() {
        // Register of local receiver to listen to found bluetooth devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i("BLUETOOTH_DETECTION_SERVICE", "receiving from bluetooth");
                long currentTimestamp = new Date().getTime();

                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    executor.execute(() -> {
                        BluetoothDeviceEntity searched = bluetoothDeviceDao.findByAddress(foundDevice.getAddress());

                        if (searched == null) {
                            // insert a new detected device into the database
                            executor.execute(() -> {
                                bluetoothDeviceDao.insert(new BluetoothDeviceEntity(foundDevice, currentTimestamp));
                                Log.i("BLUETOOTH_DETECTION_SERVICE", "added a new bluetooth device");
                            });
                        } else {

                            // if the time elapsed since last detection is below the limit that we've defined
                            // then we update and still having a connection

                            long elapsedTime = (currentTimestamp - searched.lastTimestamp);

                            if (elapsedTime <= SensorableConstants.TIME_SINCE_LAST_BLUETOOTH_DETECTION) {

                                executor.execute(() -> {
                                    searched.lastTimestamp = currentTimestamp;
                                    bluetoothDeviceDao.updateDevice(searched);
                                    Log.i(
                                            "BLUETOOTH_DETECTION_SERVICE",
                                            "updating with elapsed time " + elapsedTime / 1000 + " seconds"
                                    );
                                });

                            } else {
                                executor.execute(() -> {
                                    // a lot of time elapsed since the last detection, so this is a new discovery
                                    searched.firstTimestamp = currentTimestamp;

                                    bluetoothDeviceDao.insert(searched);
                                    Log.i(
                                            "BLUETOOTH_DETECTION_SERVICE",
                                            "inserting new with elapsed time " + elapsedTime / 1000 + " seconds"
                                    );
                                });
                            }
                        }
                    });
                }
            }
        }, filter);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Log.i("BLUETOOTH_DETECTION_SERVICE", "did discovery");
                BluetoothDevicesProvider.startDiscovery();
            }
        }, 0, SensorableConstants.SCHEDULE_BLUETOOTH_DISCOVERY, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}