package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.SensorsProvider;

import java.util.ArrayList;

public class SensorsProviderService extends Service {
    private SensorsProvider sensorsProvider;
    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer;

    public SensorsProviderService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "SENSORS PROVIDER SERVICE", Toast.LENGTH_SHORT).show();
        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeSensorsProvider() {
        sensorsProvider = new SensorsProvider(this);
        SensorEventListener transmissionListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorMessage msg =
                        new SensorTransmissionCoder.SensorMessage(
                                DeviceType.MOBILE,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sendMessageToActivity(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorsProvider.subscribeToSensor(Sensor.TYPE_PROXIMITY, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_AMBIENT_TEMPERATURE, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_RELATIVE_HUMIDITY, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_LIGHT, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_ACCELEROMETER, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sendMessageToActivity(SensorTransmissionCoder.SensorMessage msg) {
        sensorMessagesBuffer.add(msg);
        if (sensorMessagesBuffer.size() >= SensorableConstants.SENSORS_PROVIDER_SERVICE_BUFFER_SIZE) {
            Intent intent = new Intent(SensorableConstants.SENSORS_PROVIDER_SENDS_DATA);
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
        sendMessageToActivity(new SensorTransmissionCoder.SensorMessage(DeviceType.EMPATICA, sensorType, values));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }
}