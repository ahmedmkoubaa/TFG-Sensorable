package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.LoginHelper;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class DataCollectorService extends Service {

    private BroadcastReceiver sensorDataReceiver;
    private ExecutorService executor;
    private SensorMessageDao sensorMessageDao;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeMobileDatabase();
        initializeBroadcastListeners();
        Log.i("DATA COLLECTER SERVICE", "Already started");
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(this).sensorMessageDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void initializeBroadcastListeners() {
        sensorDataReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        saveSensorReads(arrayMessage);
                    }


                };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.EMPATICA_SENDS_SENSOR_DATA));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.WEAR_SENDS_SENSOR_DATA));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                sensorDataReceiver,
                new IntentFilter(SensorableConstants.SENSORS_PROVIDER_SENDS_SENSORS));
    }

    private void saveSensorReads(ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage) {
        executor.execute(() -> {
            ArrayList<SensorMessageEntity> sensorMessageEntities = new ArrayList<>();
            for (SensorTransmissionCoder.SensorMessage s : arrayMessage) {
                sensorMessageEntities.add(s.toSensorDataMessage(LoginHelper.getUserCode(this)));
            }
            sensorMessageDao.insertAll(sensorMessageEntities);
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}