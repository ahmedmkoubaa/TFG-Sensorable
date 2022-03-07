package com.sensorable;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.commons.BluetoothDevicesProvider;

import java.util.ArrayList;

public class BluetoothOptionsActivity extends AppCompatActivity {

    private ListView bluetoothFoundDevices;
    private BluetoothDevicesProvider bluetoothProvider;
    private ArrayAdapter adapter;
    private ArrayList<BluetoothDevice> bleArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_options);

        initializeBluetoothDevicesProvider();
        initializeAttributesFromUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothProvider.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                // until we find it, it's false
                boolean found = false;

                if (bleArray != null) {
                    for (BluetoothDevice b: bleArray) {
                        if (b.getAddress().equals(result.getDevice().getAddress())) {
                            found = true;
                            Log.i("BLUETOOTH_OPTIONS_ACTIVITY", "device recently added");
                            break;
                        }
                    }
                }

                if (!found) {
                    BluetoothDevice device = result.getDevice();

                    bleArray.add(result.getDevice());
                    adapter.notifyDataSetChanged();
                    Log.i("BLUETOOTH_OPTIONS_ACTIVITY", "added a new bluetooth device");

                }

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
    }

    private void initializeBluetoothDevicesProvider() {
        bluetoothProvider = new BluetoothDevicesProvider(this);
        bluetoothProvider.turnOnBluetooth();
    }
}