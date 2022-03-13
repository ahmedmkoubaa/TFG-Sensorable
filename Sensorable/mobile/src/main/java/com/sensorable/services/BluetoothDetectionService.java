package com.sensorable.services;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.concurrent.ExecutorService;

public class BluetoothDetectionService extends Service {
    private BluetoothDevicesProvider bluetoothProvider;
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
        bluetoothProvider = new BluetoothDevicesProvider(this);
        bluetoothProvider.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i("BLUETOOTH_DETECTION_SERVICE", "scanning found a result " + result.getDevice().getAddress());

              executor.execute(() -> {
                  BluetoothDeviceEntity searched = bluetoothDeviceDao.findByAddress(result.getDevice().getAddress());
                  if (searched == null) {
                      BluetoothDeviceEntity
                              databaseDevice = new BluetoothDeviceEntity();

                      BluetoothDevice device = result.getDevice();
                      databaseDevice.address = device.getAddress();
                      databaseDevice.deviceName = device.getName();
                      databaseDevice.bluetoothDeviceType = device.getBluetoothClass().getDeviceClass();
                      databaseDevice.bondState = device.getBondState();

                      // update database
                      bluetoothDeviceDao.insert(databaseDevice);


                      Log.i("BLUETOOTH_DETECTION_SERVICE", "added a new bluetooth device");
                  }
              });
            }
        });
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
}