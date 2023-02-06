package com.commons.devicesDetection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothDevicesProvider {

    public static final int SELECT_DEVICE_REQUEST_CODE = 0;
    public static final int REQUEST_ENABLE_BT = 1;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public static void startDiscovery() {
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    public BluetoothDevicesProvider(Context context) {
        this.context = context;
        initializeBluetoothDetection();
    }

    public static boolean isEnabledBluetooth() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static void enableBluetooth(AppCompatActivity activity) {
        if (!isEnabledBluetooth()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void onActivityResultTurnOnBluetooth(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth activado correctamente", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeBluetoothDetection() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startScan(ScanCallback callback) {
        if (isEnabledBluetooth()) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(callback);
        }
    }

    public void startScan() {
        startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.i("BLUETOOTH_DEVICES_PROVIDER", "scan found devices");
            }
        });

    }

    public void stopScan() {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(new ScanCallback() {
            // we use parent methods
        });
    }


    public void onActivityResultCompanionFoundDevice(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            Log.i("BLUETOOTH_DETECTOR_PROVIDER", "DEVICE FOUND, LET'S TRY BOUND");
            BluetoothDevice deviceToPair = data.getParcelableExtra(
                    CompanionDeviceManager.EXTRA_DEVICE
            );

            if (deviceToPair != null) {
                Log.i("BLUETOOTH_DETECTOR_PROVIDER", "Bluetooth class " + deviceToPair.getBluetoothClass());

                deviceToPair.createBond();
                deviceToPair.getBondState();
                // ... Continue interacting with the paired device.

            }
        }
    }
}
