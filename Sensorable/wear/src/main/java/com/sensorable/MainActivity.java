package com.sensorable;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.commons.services.CsvSaverService;
import com.commons.utils.DeviceType;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorablePermissions;
import com.commons.utils.SensorsProvider;
import com.commons.services.SensorsProviderService;
import com.sensorable.services.DataSenderService;
import com.sensorable.utils.LoggerAdapter;
import com.sensorable.utils.SensorableLogger;
import com.sensorable.utils.WearSensorDataSender;
import com.sensorable.utils.WearosEnvironment;

public class MainActivity extends WearableActivity {
    private final int[] listenedSensors = {
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE
    };

    private LoggerAdapter loggerAdapter;

    private SensorsProvider sensorsProvider;
    private WearSensorDataSender sensorSender;

    private ListView loggerList;
    private ToggleButton rightHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request all necessary permissions
        SensorablePermissions.requestAll(this);

        initializeServices();

        initializeListenersForUI();
        initializeReminders();

        initializeSensorsDataSendingListeners();
    }

    private void initializeServices() {
        startService(new Intent(this, DataSenderService.class));

        Intent intent = new Intent(this, SensorsProviderService.class);
        intent.putExtra(SensorableConstants.SENSORS_PROVIDER_DEVICE_TYPE, WearosEnvironment.getDeviceType());
        startService(intent);

        startService(new Intent(this, CsvSaverService.class));
    }

    // initialize a sensors data receptor
    private void initializeSensorsDataSendingListeners() {
        sensorsProvider = new SensorsProvider(this);
        sensorSender = new WearSensorDataSender(this);

        SensorEventListener listenerDataSender = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorSender.sendMessage(WearosEnvironment.getDeviceType(), sensorEvent.sensor.getType(), sensorEvent.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        for (int sensorCode : listenedSensors) {
            sensorsProvider.subscribeToSensor(sensorCode, listenerDataSender, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // remind to show the logging messages when sent some information data
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

    // this is pure front end and can stay right here
    private void initializeListenersForUI() {
        loggerAdapter = new LoggerAdapter(getBaseContext(), R.layout.logger_message_layout, SensorableLogger.getLoggedData());
        loggerAdapter.setNotifyOnChange(true);

        loggerList = (ListView) findViewById(R.id.loggerList);
        loggerList.setAdapter(loggerAdapter);

        rightHand = (ToggleButton) findViewById(R.id.rightHand);
        rightHand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WearosEnvironment.setDeviceType(isChecked ? DeviceType.WEAROS_RIGHT_HAND : DeviceType.WEAROS_LEFT_HAND);
            }
        });
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