package com.sensorable.activities;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.devicesDetection.BluetoothDevicesProvider;
import com.sensorable.R;
import com.sensorable.utils.BluetoothDeviceAdapter;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class BluetoothOptionsActivity extends AppCompatActivity {

    private ListView bluetoothFoundDevices;
    private BluetoothDevicesProvider bluetoothProvider;
    private BluetoothDeviceAdapter adapter;
    private ArrayList<BluetoothDeviceEntity> bleArray;

    private BluetoothDeviceDao bluetoothDeviceDao;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_options);

        initializeDatabase();
        initializeBluetoothDevicesProvider();
        initializeAttributesFromUI();
    }

    private void initializeDatabase() {
        bluetoothDeviceDao = MobileDatabaseBuilder.getDatabase(this).bluetoothDeviceDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothProvider.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

/*                BluetoothDeviceEntity searched = bluetoothDeviceDao.findByAddress(result.getDevice().getAddress());
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

                    // update list representation
                    bleArray.add(databaseDevice);

                    // data changed, it's important to notify this event
                    adapter.notifyDataSetChanged();

                    Log.i("BLUETOOTH_SCANNER", "added a new bluetooth device");
                }*/

                updateFromDatabase();

            }
        });
    }

    private void initializeAttributesFromUI() {
        bluetoothFoundDevices = (ListView) findViewById(R.id.foundDevices);
        bleArray = new ArrayList<>();

        adapter = new BluetoothDeviceAdapter(
                this,
                R.layout.bluetooth_devices_layout,
                bleArray
        );

        adapter.setNotifyOnChange(true);
        bluetoothFoundDevices.setAdapter(adapter);

        updateFromDatabase();
    }

    private void updateFromDatabase() {
        executor.execute(() -> {
            bleArray.clear();
            bleArray.addAll(bluetoothDeviceDao.getAll());
        });
    }

    private void initializeBluetoothDevicesProvider() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        BluetoothDevicesProvider.enableBluetooth(this);
    }
}