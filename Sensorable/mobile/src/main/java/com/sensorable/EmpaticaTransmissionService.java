package com.sensorable;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;
import com.example.commons.SensorDataMessage;

import java.security.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmpaticaTransmissionService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {

    private EmpaDeviceManager deviceManager;
    private static final String EMPATICA_API_KEY = "e910f7a73ce74dbd99b774b9f6010ab5";
    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "EMPATICA", Toast.LENGTH_SHORT).show();


        // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
        deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

        // Initialize the Device Manager using your API key. You need to have Internet access at this point.
        deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);

        return super.onStartCommand(intent, flags, startId);
    }


    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("EmpaticaDataUpdates");
        // You can also include some extra data.

        Bundle sensorMesssages = new Bundle();
        sensorMesssages.putString("EmpaticaMessage", msg);

        intent.putExtra("EMPATICA_DATA_COLLECTED", sensorMesssages);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {

    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {

    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {

    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {
//        Toast.makeText(this, "recibo temperatura " + t, Toast.LENGTH_SHORT).show();
//        stepCounterText.setText("temperatura " + t);
//        updateLabel(stepCounterText, "t: " + t);

//        Toast.makeText(this, "t: " + t, Toast.LENGTH_SHORT).show();
        sendMessageToActivity("temp: " + t);
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
//        Toast.makeText(this, "recibo acelerómetro " + x + " " + y + " " + z, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didReceiveBatteryLevel(float level, double timestamp) {
        sendMessageToActivity("batería " + level);
//        Toast.makeText(MainActivity.this, "receiving battery " + level, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }


    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
//        Toast.makeText(MainActivity.this, "UpdateState: " + status.name(), Toast.LENGTH_LONG).show();
//        updateLabel(stepCounterText, status.name());

//        Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            Toast.makeText(this,  status.name() + " - Turn on your device", Toast.LENGTH_SHORT).show();

            // Start scanning
            deviceManager.startScanning();

//            Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
//            updateLabel(stepCounterText, status.name());


            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
//            Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
//            updateLabel(stepCounterText, status.name());
            // The device manager disconnected from a device

        } else if (status == EmpaStatus.DISCONNECTED) {
//            updateLabel(stepCounterText, status.name());
//            Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void didEstablishConnection() {

    }

    @Override
    public void didUpdateSensorStatus(int status, EmpaSensorType type) {

    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php

        Toast.makeText(this, "didDiscoverDevice " + deviceName + " allowed: " + allowed, Toast.LENGTH_SHORT).show();

        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
//                toast("connected yes");
                deviceManager.connectDevice(bluetoothDevice);
                Toast.makeText(this, "connected yes", Toast.LENGTH_SHORT).show();
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didFailedScanning(int errorCode) {
        Toast.makeText(this, "NO SE ENCONTRÓ NADA " + errorCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void didRequestEnableBluetooth() {

    }

    @Override
    public void bluetoothStateChanged() {

    }

    @Override
    public void didUpdateOnWristStatus(int status) {

    }
}
