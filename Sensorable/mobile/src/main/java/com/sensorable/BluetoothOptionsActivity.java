package com.sensorable;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class BluetoothOptionsActivity extends AppCompatActivity {

    private ListView bluetoothFoundDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_options);

        bluetoothFoundDevices = (ListView) findViewById(R.id.foundDevices);
        ArrayList<BluetootDetectedDevice> bleArray = new ArrayList<BluetootDetectedDevice>();
        bleArray.add(new BluetootDetectedDevice("carmenito", "00:14:25:FF", false));
        bleArray.add(new BluetootDetectedDevice("carmenito", "00:14:25:FF", true));
        bleArray.add(new BluetootDetectedDevice("carmenito", "00:14:25:FF", false));
        bleArray.add(new BluetootDetectedDevice("carmenito", "00:14:25:FF", true));
        bleArray.add(new BluetootDetectedDevice("carmenito", "00:14:25:FF", false));


        ArrayAdapter adapter = new BluetoothDeviceAdapter(this, R.layout.bluetooth_devices_layout, bleArray);
        bluetoothFoundDevices.setAdapter(adapter);

    }
}