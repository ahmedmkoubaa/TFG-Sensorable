package com.sensorable;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.commons.utils.Actions;
import com.commons.utils.DeviceType;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorablePermissions;
import com.commons.utils.ServiceState;
import com.commons.utils.ServiceStatePreferences;
import com.sensorable.services.WearForegroundService;
import com.sensorable.utils.LoggerAdapter;
import com.sensorable.utils.SensorableLogger;
import com.sensorable.utils.WearosEnvironment;

public class MainActivity extends WearableActivity {
    private LoggerAdapter loggerAdapter;
    private ToggleButton rightHand;
    private ListView loggerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // request all necessary permissions
        SensorablePermissions.requestAll(this);

        initializeForegroundService(Actions.START);

        initializeListenersForUI();
        initializeReminders();

    }

    private void initializeForegroundService(Actions action) {
        if (ServiceStatePreferences.getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) {
            return;
        }

        Intent intent = new Intent(this, WearForegroundService.class);
        intent.setAction(action.name()); // Set the action to be performed (Start or Stop)
        startForegroundService(intent); // Start a foreground service
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

        loggerList = findViewById(R.id.loggerList);
        loggerList.setAdapter(loggerAdapter);

        rightHand = findViewById(R.id.rightHand);
        rightHand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WearosEnvironment.setDeviceType(isChecked ? DeviceType.WEAROS_RIGHT_HAND : DeviceType.WEAROS_LEFT_HAND);
            }
        });
    }
}