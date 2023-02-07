package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.LoginHelper;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.SensorableIntentFilters;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class StorageService extends Service {
    private final Handler handler = new Handler();
    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;
    private BroadcastReceiver sensorDataReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BACKUP_SERVICE", "started backup service correctly");

        initializeMobileDatabase();
        initializeBroadcastListeners();
        initializeReminders();

        return super.onStartCommand(intent, flags, startId);
    }

    // initialize the broadcast receivers
    private void initializeBroadcastListeners() {

        // To receive data and store it using local database
        sensorDataReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        saveSensorReads(arrayMessage);
                    }


                };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.WEAR_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.MOBILE_SENSORS);
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

    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(StorageService.this).sensorMessageDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    // the service wakes up to send data and then it goes to sleep again until the alarm fires again
    private void initializeReminders() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendDataToMqttBroker();
                handler.postDelayed(this, SensorableConstants.SCHEDULE_DATABASE_BACKUP);
            }
        };

        handler.post(runnable);
    }

    // This function sends data in chunks via mqtt to remote services
    private void sendDataToMqttBroker() {
        // establish connection with the mqtt broker
        MqttHelper.connect();

        executor.execute(() -> {
            List<SensorMessageEntity> sensorsData = null;

            try {
                // retrieve all data from local database
                sensorsData = sensorMessageDao.getAll();
            } catch (Exception e) {
                Log.i("Sensors-back-up-service", "error getting the sensorMessageDao");
            }

            // check if there is data to send
            if (sensorsData != null && !sensorsData.isEmpty()) {

                // split data in chunks to send them easily
                final double parts = Math.ceil(sensorsData.size() / SensorableConstants.BACKUP_PART_SIZE);

                for (int j = 1; j <= parts; j++) {
                    String payload = "[ ";

                    for (int i = (int) ((j - 1) * parts); i < sensorsData.size() && i < j * SensorableConstants.BACKUP_PART_SIZE; i++) {
                        payload += sensorsData.get(i).toJson() + ",";
                    }

                    payload = payload.substring(0, payload.length() - 1) + "]";

                    MqttHelper.publish(SensorableConstants.MQTT_SENSORS_INSERT, payload.getBytes());
                    Log.i("BACK UP SERVICE", payload);
                }

                // delete all the sent data
                sensorMessageDao.deleteAll();
            } else {
                Log.i("BACK UP SERVICE", "no rows to back up, see you soon");
            }

        });


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}