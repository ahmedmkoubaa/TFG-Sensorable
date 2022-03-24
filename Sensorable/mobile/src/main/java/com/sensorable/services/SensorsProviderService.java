package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
        sensorMessagesBuffer = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "SENSORS PROVIDER SERVICE", Toast.LENGTH_SHORT).show();

        initializeSensorsProvider();

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

                broadcastSensorMessages(msg);
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

        sensorsProvider.subscribeToGps(new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
               broadcastGPSLocation(location);
               Log.i("SENSORS_PROVIDER_SERVICE", "location update");
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        });
    }

    private void broadcastGPSLocation(Location location) {
        Intent intent = new Intent(SensorableConstants.SENSORS_PROVIDER_SENDS_LOCATION);

        Bundle bundle = new Bundle();
        bundle.putParcelable(SensorableConstants.BROADCAST_LOCATION, location);

        intent.putExtra(SensorableConstants.EXTRA_MESSAGE, bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastSensorMessages(SensorTransmissionCoder.SensorMessage msg) {
        sensorMessagesBuffer.add(msg);
        if (sensorMessagesBuffer.size() >= SensorableConstants.SENSORS_PROVIDER_SERVICE_BUFFER_SIZE) {
            Intent intent = new Intent(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS);

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE, new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra(SensorableConstants.EXTRA_MESSAGE, bundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
    }

    private void broadcastSensorMessages(int sensorType, float[] values) {
        broadcastSensorMessages(new SensorTransmissionCoder.SensorMessage(DeviceType.EMPATICA, sensorType, values));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }
}