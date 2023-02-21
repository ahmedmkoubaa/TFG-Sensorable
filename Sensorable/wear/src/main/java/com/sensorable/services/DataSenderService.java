package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableDatabase;
import com.commons.utils.SensorableIntentFilters;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.sensorable.utils.WearDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class DataSenderService extends Service {
    private SensorableDatabase database;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PutDataMapRequest dataMap = PutDataMapRequest.create("/data");
        dataMap.getDataMap().putString("message", "Hello from Wear OS!");
        Task<DataItem> putDataTask = Wearable.getDataClient(getApplicationContext()).putDataItem(dataMap.asPutDataRequest());


        initializeDatabase();
        return super.onStartCommand(intent, flags, startId);
    }


    private void initializeDatabase() {
        database = WearDatabaseBuilder.getDatabase(this);
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

                            }


                        }, SensorableIntentFilters.SENSORS_PROVIDER_SENSORS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}