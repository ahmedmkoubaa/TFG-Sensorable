package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.DeviceType;
import com.commons.utils.EmpaticaSensorType;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import java.util.ArrayList;

public class EmpaticaTransmissionService extends Service implements EmpaDataDelegate, EmpaStatusDelegate {
    private static final String EMPATICA_API_KEY = "e910f7a73ce74dbd99b774b9f6010ab5";
    private ArrayList<SensorTransmissionCoder.SensorData> sensorMessagesBuffer;
    private EmpaDeviceManager deviceManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
           if (deviceManager == null) {
               // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
               deviceManager = new EmpaDeviceManager(this, this, this);

               // Initialize the Device Manager using your API key. You need to have Internet access at this point.
               deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
           }

        } catch (UnsatisfiedLinkError e) {
            Log.e("EMPATICA_TRANSMISSION_SERVICE", "error linking the empatica api");
        } catch (NoClassDefFoundError e) {
            Log.e("EMPATICA_TRANSMISSION_SERVICE", "error importing the EmpaDeviceManager");
        }

        // Initialize array buffer to send a bunch of messages instead of doing a single sending per sensor read
        sensorMessagesBuffer = new ArrayList<>();


        return super.onStartCommand(intent, flags, startId);
    }


    private void sendMessageToActivity(SensorTransmissionCoder.SensorData msg) {
        sensorMessagesBuffer.add(msg);
        if (sensorMessagesBuffer.size() >= SensorableConstants.EMPATICA_BUFFER_SIZE) {
            Intent intent = new Intent(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA);
            // You can also include some extra data.

            Bundle empaticaBundle = new Bundle();
            empaticaBundle.putParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE, new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra(SensorableConstants.EXTRA_MESSAGE, empaticaBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
    }

    private void sendMessageToActivity(int sensorType, float[] values) {
        sendMessageToActivity(new SensorTransmissionCoder.SensorData(DeviceType.EMPATICA, sensorType, values));
    }

    private void sendInfoMessage(String msg) {
        Intent intent = new Intent(SensorableConstants.SERVICE_SENDS_INFO);
        intent.putExtra(SensorableConstants.EXTRA_MESSAGE, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Start scanning
        if (deviceManager != null) {
            deviceManager.startScanning();
        }

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            sendInfoMessage("Enciende la Empatica");

            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {

            sendInfoMessage("Empatica se ha conectado");
            deviceManager.stopScanning();
            // The device manager connected to a device

        } else if (status == EmpaStatus.DISCONNECTED) {
            // The device manager manager disconnected from a device
            sendInfoMessage("Empatica se ha desconectado");
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php

        sendInfoMessage("Encontrada " + deviceName);

        if (allowed) {
            // Stop scanning. The first allowed device will do.
//            deviceManager.stopScanning();

            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);

            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                sendInfoMessage("Conexión no permitida con este dispositivo");
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        float[] values = {gsr};
        sendMessageToActivity(Sensor.TYPE_RELATIVE_HUMIDITY, values);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        float[] values = {bvp};
        sendMessageToActivity(EmpaticaSensorType.BVP, values);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        float[] values = {60 / ibi};
        sendMessageToActivity(Sensor.TYPE_HEART_RATE, values);
        sendMessageToActivity(EmpaticaSensorType.IBI, values);
    }

    @Override
    public void didReceiveTemperature(float t, double timestamp) {
        float[] values = {t};
        sendMessageToActivity(Sensor.TYPE_AMBIENT_TEMPERATURE, values);
    }

    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        float[] values = {x, y, z};
        sendMessageToActivity(Sensor.TYPE_LINEAR_ACCELERATION, values);
    }

    @Override
    public void didReceiveBatteryLevel(float level, double timestamp) {
        float[] values = {level};
        sendMessageToActivity(EmpaticaSensorType.BATTERY_LEVEL, values);
    }

    @Override
    public void didReceiveTag(double timestamp) {
        sendInfoMessage("Conexión estable");
    }

    @Override
    public void didEstablishConnection() {
        sendInfoMessage("Conexión establecida!!");
    }

    @Override
    public void didUpdateSensorStatus(int status, EmpaSensorType type) {
        sendInfoMessage("Sensores actualizados");
    }

    @Override
    public void didFailedScanning(int errorCode) {
        sendInfoMessage("Volviendo a escanear ...");
    }

    @Override
    public void didRequestEnableBluetooth() {
        sendInfoMessage("Enciende el Bluetooth");
    }

    @Override
    public void bluetoothStateChanged() {
    }

    @Override
    public void didUpdateOnWristStatus(int status) {
    }
}
