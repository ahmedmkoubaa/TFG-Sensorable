package com.commons.services;

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
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.DeviceType;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorsProvider;

import java.util.ArrayList;

public class SensorsProviderService extends Service {
    private final ArrayList<SensorTransmissionCoder.SensorData> sensorMessagesBuffer;


    private SensorsProvider sensorsProvider;

    public SensorsProviderService() {
        sensorMessagesBuffer = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        initializeSensorsProvider(intent.getIntExtra(SensorableConstants.SENSORS_PROVIDER_DEVICE_TYPE, -1));

        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeSensorsProvider(final int deviceTypeFromIntent) {
        final int deviceType = deviceTypeFromIntent;

        sensorsProvider = new SensorsProvider(this);
        SensorEventListener transmissionListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorData msg =
                        new SensorTransmissionCoder.SensorData(
                                deviceType,
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                broadcastSensorMessages(msg);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };


        for (Pair<Integer, String> sensorCode : SensorableConstants.LISTENED_SENSORS) {
            sensorsProvider.subscribeToSensor(sensorCode.first, transmissionListener, SensorManager.SENSOR_DELAY_NORMAL);
        }

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
        // broadcast the location as a location to a custom intent filter
        Intent intent = new Intent(SensorableConstants.SENSORS_PROVIDER_SENDS_LOCATION);

        Bundle bundle = new Bundle();
        bundle.putParcelable(SensorableConstants.BROADCAST_LOCATION, location);

        intent.putExtra(SensorableConstants.EXTRA_MESSAGE, bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // broadcast the locations as a sensor to save it in the bd and to use it in the cloud
        SensorTransmissionCoder.SensorData msg =
                new SensorTransmissionCoder.SensorData(
                        DeviceType.MOBILE,
                        SensorableConstants.SENSOR_GPS,
                        new float[]{
                                (float) location.getAltitude(),
                                (float) location.getLatitude(),
                                (float) location.getLongitude()
                        }
                );

        broadcastSensorMessages(msg);
    }

    private void broadcastSensorMessages(SensorTransmissionCoder.SensorData msg) {
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
        broadcastSensorMessages(new SensorTransmissionCoder.SensorData(DeviceType.MOBILE, sensorType, values));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}