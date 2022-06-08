package com.sensorable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.commons.SensorablePermissions;
import com.commons.SensorsProvider;


public class MainActivity extends WearableActivity {
    private final int[] listenedSensors = {
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_LINEAR_ACCELERATION
    };

    private TextView heartText;
    private TextView lightText;
    private SensorsProvider sensorsProvider;
    private Button send, sendStepCounter;
    private WearSensorDataSender sensorSender;
    private float[] lastHeartRateValue;

    private SensorEventListener heartRateListener;
    private SensorEventListener stepCounterListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request all necessary permissions
        SensorablePermissions.requestAll(this);

        heartText = findViewById(R.id.heartRateText);
        lightText = findViewById(R.id.temperatureText);
        send = findViewById(R.id.buttonSendHeartRate);
        sendStepCounter = findViewById(R.id.buttonSendStepCounter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorSender.sendMessage(Sensor.TYPE_HEART_RATE, lastHeartRateValue);
            }
        });

        sendStepCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float[] value = new float[1];
                value[0] = 12312;
                Toast.makeText(MainActivity.this, "enviando step counter", Toast.LENGTH_LONG).show();
                sensorSender.sendMessage(Sensor.TYPE_STEP_COUNTER, value);
            }
        });


        sensorsProvider = new SensorsProvider(this);
        sensorSender = new WearSensorDataSender(this);

        initializeListenersForUI();
        initializeSensorsDataSendingListeners();

    }

    private void initializeSensorsDataSendingListeners() {
        SensorEventListener listenerDataSender = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorSender.sendMessage(sensorEvent.sensor.getType(), sensorEvent.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };


        for (int sensorCode : listenedSensors) {
            sensorsProvider.subscribeToSensor(sensorCode, listenerDataSender, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initializeListenersForUI() {
        heartRateListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                heartText.setText(Math.round(sensorEvent.values[0]) + " ppm");
                lastHeartRateValue = sensorEvent.values;

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        stepCounterListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                lightText.setText(Math.round(sensorEvent.values[0]) + " pasos");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        sensorsProvider.subscribeToSensor(Sensor.TYPE_HEART_RATE, heartRateListener, SensorManager.SENSOR_DELAY_NORMAL);
        sensorsProvider.subscribeToSensor(Sensor.TYPE_STEP_COUNTER, stepCounterListener, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        sensorsProvider.unsubscribeToSensor(heartRateListener);
        sensorsProvider.unsubscribeToSensor(stepCounterListener);
    }
}