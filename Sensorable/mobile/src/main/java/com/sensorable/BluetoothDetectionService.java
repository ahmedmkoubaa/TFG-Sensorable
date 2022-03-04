package com.sensorable;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static androidx.core.app.ActivityCompat.startIntentSenderForResult;

public class BluetoothDetectionService extends Service {
    private static final int SELECT_DEVICE_REQUEST_CODE = 0;

    public BluetoothDetectionService() {
    }


    private void initializeBluetoothDetection() {
        CompanionDeviceManager deviceManager =
                (CompanionDeviceManager) getSystemService(
                        Context.COMPANION_DEVICE_SERVICE
                );


        BluetoothDeviceFilter deviceFilter =
                new BluetoothDeviceFilter.Builder().build();

        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false)
                .build();

        deviceManager.associate(pairingRequest,
                new CompanionDeviceManager.Callback() {
                    @Override
                    public void onDeviceFound(IntentSender chooserLauncher) {
                        Log.i("BLUETOOTH_DETECTOR", "found a device");
                        try {
                            AppCompatActivity.startIntentSenderForResult(chooserLauncher,
                                    SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            // failed to send the intent
                        }
                    }

                    @Override
                    public void onFailure(CharSequence error) {
                        // handle failure to find the companion device
                        Log.i("BLUETOOTH_DETECTOR", "error trying to found a device");
                    }
                }, null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SELECT_DEVICE_REQUEST_CODE) {
            Toast.makeText(this, "Found a device", Toast.LENGTH_SHORT).show();
            if (resultCode == Activity.RESULT_OK && data != null) {
                Toast.makeText(this, "It went all well", Toast.LENGTH_SHORT).show();

                BluetoothDevice deviceToPair = data.getParcelableExtra(
                        CompanionDeviceManager.EXTRA_DEVICE
                );

                if (deviceToPair != null) {
                    Toast.makeText(this, "We can do a bond", Toast.LENGTH_SHORT).show();

                    deviceToPair.createBond();
                    // ... Continue interacting with the paired device.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}