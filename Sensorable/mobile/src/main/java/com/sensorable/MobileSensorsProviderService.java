package com.sensorable;

import android.app.Activity;
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

import com.example.commons.DeviceType;
import com.example.commons.SensorTransmissionCoder;
import com.example.commons.SensorsProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MobileSensorsProviderService extends Service {
    private final static int MAX_BUFFER_SIZE = 512;

    private SensorsProvider sensorsProvider;
    private Activity activity;
    private ArrayList<SensorTransmissionCoder.SensorMessage> sensorMessagesBuffer;
    private int [] sensors;
    private int delay;

    public MobileSensorsProviderService() {

    }
    public MobileSensorsProviderService(WeakReference<Activity> activityRef, int [] sensors, int delay) {
        super();
        this.activity = activityRef.get();
        this.sensors = sensors;
        this.delay = delay;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (this.activity == null) {
            Log.i("NULL_ACTIVITY", "We have received a null activity something went wrong");
        }
        sensorsProvider = new SensorsProvider(this.activity);
        sensorMessagesBuffer = new ArrayList<>();
        Toast.makeText(this, "MOBILE SENSORS PROVIDER SERVICE", Toast.LENGTH_SHORT).show();

        initializeSensors();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeSensors() {


        SensorEventListener sensorsSenderListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Log.i("MOBILE_SENSORS_PROVIDER", "Received sensor update " + sensorEvent.sensor.getType());
                sendMessageToActivity(sensorEvent.sensor.getType(), sensorEvent.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        for (int sensorType: sensors) {
            sensorsProvider.subscribeToSensor(sensorType, sensorsSenderListener, delay);
        }
    }

    private void sendMessageToActivity(SensorTransmissionCoder.SensorMessage msg) {

        sensorMessagesBuffer.add(msg);
        if (sensorMessagesBuffer.size() >= MAX_BUFFER_SIZE) {
            Intent intent = new Intent("MobileSensorsProviderUpdates");
            // You can also include some extra data.

            Bundle sensorsBundle = new Bundle();
            sensorsBundle.putParcelableArrayList("SensorsProviderMessage", new ArrayList<>(sensorMessagesBuffer));

            intent.putExtra("SENSORS_PROVIDER_DATA_COLLECTED", sensorsBundle);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            // reset buffer
            sensorMessagesBuffer.clear();
        }
    }

    private void sendMessageToActivity(int sensorType, float[] values) {
        sendMessageToActivity(new SensorTransmissionCoder.SensorMessage(DeviceType.MOBILE, sensorType, values));
    }

    private void sendInfoMessage(String msg) {
        Intent intent = new Intent("INFO");
        intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}