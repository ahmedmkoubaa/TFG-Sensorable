package com.commons.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.database.SensorMessageEntity;
import com.commons.utils.CsvSaver;
import com.commons.utils.DeviceType;
import com.commons.utils.LoginHelper;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableIntentFilters;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CsvSensorsSaverService extends Service {
    private final ArrayList<SensorMessageEntity> sensorBufferToExportCSV = new ArrayList();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeReceiver();
        return super.onStartCommand(intent, flags, startId);

    }

    private void initializeReceiver() {
        // To receive data and store it using local database
        final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                ArrayList<SensorTransmissionCoder.SensorData> sensorsArray = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);

                sensorBufferToExportCSV.addAll(SensorTransmissionCoder.SensorData.toSensorDataMessages(sensorsArray));

                if (sensorBufferToExportCSV.size() > SensorableConstants.MAX_COLLECTED_DATA_EXPORT_CSV) {
                    CsvSaver.exportToCsv(sensorBufferToExportCSV, Objects.toString(LoginHelper.getUserCode(getApplicationContext()), "NULL"));
                    sensorBufferToExportCSV.clear();

                }
            }
        };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.SERVICE_PROVIDER_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.WEAR_SENSORS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}