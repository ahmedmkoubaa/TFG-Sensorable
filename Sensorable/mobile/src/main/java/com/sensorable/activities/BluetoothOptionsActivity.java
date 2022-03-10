package com.sensorable.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.sensorable.utils.BluetoothDeviceAdapter;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.R;

import java.util.ArrayList;

public class BluetoothOptionsActivity extends AppCompatActivity {

    private ListView bluetoothFoundDevices;
    private BluetoothDevicesProvider bluetoothProvider;
    private BluetoothDeviceAdapter adapter;
    private ArrayList<com.commons.database.BluetoothDevice> bleArray;

    private BluetoothDeviceDao bluetoothDeviceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_options);

        initializeDatabase();
        initializeBluetoothDevicesProvider();
        initializeAttributesFromUI();
    }

    private void initializeDatabase() {
        MobileDatabase database = Room.databaseBuilder(
                getApplicationContext(),
                MobileDatabase.class,
                SensorableConstants.MOBILE_DATABASE_NAME)
          .allowMainThreadQueries()
         .build();

        bluetoothDeviceDao = database.bluetoothDeviceDao();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothProvider.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                com.commons.database.BluetoothDevice searched = bluetoothDeviceDao.findByAddress(result.getDevice().getAddress());
                if (searched == null) {
                    com.commons.database.BluetoothDevice
                            databaseDevice = new com.commons.database.BluetoothDevice();

                    BluetoothDevice device = result.getDevice();
                    databaseDevice.address = device.getAddress();
                    databaseDevice.deviceName = device.getName();
                    databaseDevice.bluetoothDeviceType = device.getBluetoothClass().getDeviceClass();
                    databaseDevice.bondState = device.getBondState();

                    // update database
                    bluetoothDeviceDao.insert(databaseDevice);

                    // update list representation
                    bleArray.add(databaseDevice);

                    // data changed, it's important to notify this event
                    adapter.notifyDataSetChanged();

                    Log.i("BLUETOOTH_SCANNER", "added a new bluetooth device");
                }


            }
        });
    }

    private void initializeAttributesFromUI() {
        bluetoothFoundDevices = (ListView) findViewById(R.id.foundDevices);
        bleArray = new ArrayList<>(bluetoothDeviceDao.getAll());

        adapter = new BluetoothDeviceAdapter(
                this,
                R.layout.bluetooth_devices_layout,
                bleArray
        );

        adapter.setNotifyOnChange(true);
        bluetoothFoundDevices.setAdapter(adapter);
    }

    private void initializeBluetoothDevicesProvider() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        bluetoothProvider.turnOnBluetooth();
    }
}