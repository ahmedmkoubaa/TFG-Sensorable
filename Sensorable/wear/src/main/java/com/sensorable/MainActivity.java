package com.sensorable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.widget.ListView;

import com.commons.SensorableConstants;
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

    private LoggerAdapter loggerAdapter;

    private SensorsProvider sensorsProvider;
    private WearSensorDataSender sensorSender;

    private ListView loggerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request all necessary permissions
        SensorablePermissions.requestAll(this);


        sensorsProvider = new SensorsProvider(this);
        sensorSender = new WearSensorDataSender(this);

        initializeListenersForUI();
        initializeSensorsDataSendingListeners();
        initializeReminders();

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

    private void initializeReminders() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // do something
                loggerAdapter.notifyDataSetChanged();
                handler.postDelayed(this, SensorableConstants.SCHEDULE_LOGGER_REFRESH);  // 1 second delay
            }
        };
        handler.post(runnable);
    }

    private void initializeListenersForUI() {
        loggerAdapter = new LoggerAdapter(getBaseContext(), R.layout.logger_message_layout, SensorableLogger.get());
        loggerAdapter.setNotifyOnChange(true);

        loggerList = (ListView) findViewById(R.id.loggerList);
        loggerList.setAdapter(loggerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}