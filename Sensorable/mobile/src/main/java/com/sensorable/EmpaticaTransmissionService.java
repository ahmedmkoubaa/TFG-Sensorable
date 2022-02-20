package com.sensorable;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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
import com.example.commons.DeviceType;
import com.example.commons.EmpaticaSensorType;
import com.example.commons.SensorTransmissionCoder;

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


    private void sendMessageToActivity(SensorTransmissionCoder.SensorMessage msg) {
        Intent intent = new Intent("EmpaticaDataUpdates");
        // You can also include some extra data.

        Bundle sensorMesssages = new Bundle();
        sensorMesssages.putParcelable("EmpaticaMessage", msg);

        intent.putExtra("EMPATICA_DATA_COLLECTED", sensorMesssages);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageToActivity(int sensorType, float[] values) {
        sendMessageToActivity(new SensorTransmissionCoder.SensorMessage(DeviceType.EMPATICA, sensorType, values));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        float values[] = {gsr};
        sendMessageToActivity(EmpaticaSensorType.GSR, values);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        float values[] = {bvp};
        sendMessageToActivity(EmpaticaSensorType.BVP, values);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        float values[] = {ibi};
        sendMessageToActivity(EmpaticaSensorType.IBI, values);
    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {
        float values[] = {t};
        sendMessageToActivity(EmpaticaSensorType.TEMPERATURE, values);
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        float values[] = {x, y, z};
        sendMessageToActivity(EmpaticaSensorType.ACCELEROMETER, values);
    }

    @Override
    public void didReceiveBatteryLevel(float level, double timestamp) {
        float values[] = {level};
        sendMessageToActivity(EmpaticaSensorType.BATTERY_LEVEL, values);
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }


    @Override
    public void didUpdateStatus(EmpaStatus status) {

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            Toast.makeText(this,  status.name() + " - Turn on your device", Toast.LENGTH_SHORT).show();

            // Start scanning
            deviceManager.startScanning();

            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
//            Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
            // The device manager connected to a device

        } else if (status == EmpaStatus.DISCONNECTED) {
//            Toast.makeText(this, status.name(), Toast.LENGTH_SHORT).show();
            // The device manager manager disconnected from a device
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
