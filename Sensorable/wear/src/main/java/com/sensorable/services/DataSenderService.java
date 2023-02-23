package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.commons.utils.DeviceType;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableDatabase;
import com.commons.utils.SensorableIntentFilters;
import com.opencsv.CSVWriter;
import com.sensorable.utils.WearDatabaseBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DataSenderService extends Service {
    private SensorableDatabase database;
    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        PutDataMapRequest dataMap = PutDataMapRequest.create("/data");
//        dataMap.getDataMap().putString("message", "Hello from Wear OS!");
//        Task<DataItem> putDataTask = Wearable.getDataClient(getApplicationContext()).putDataItem(dataMap.asPutDataRequest());


        initializeDatabase();
        initializeReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeDatabase() {
        database = WearDatabaseBuilder.getDatabase(this);
        sensorMessageDao = database.sensorMessageDao();
        executor = WearDatabaseBuilder.getExecutor();
    }

    private void initializeReceiver() {
        // To receive data and store it using local database
        LocalBroadcastManager.getInstance(this).
                registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                                ArrayList<SensorTransmissionCoder.SensorData> sensorsArray = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                                executor.execute(() -> {
                                    ArrayList<SensorMessageEntity> newSensors = SensorTransmissionCoder.SensorData.toSensorDataMessages(sensorsArray);
                                    sensorMessageDao.insertAll(newSensors);
                                });

                                Log.i("DATA SENDER SERVICE", "received sensors size is" + sensorsArray.size());

                            }


                        }, SensorableIntentFilters.SERVICE_PROVIDER_SENSORS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}