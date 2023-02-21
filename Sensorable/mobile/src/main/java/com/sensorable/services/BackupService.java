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

import com.commons.utils.LoginHelper;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableIntentFilters;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BackupService extends Service {
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
                        ArrayList<SensorTransmissionCoder.SensorData> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        saveSensorReads(arrayMessage);
                    }


                };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.WEAR_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(sensorDataReceiver, SensorableIntentFilters.SENSORS_PROVIDER_SENSORS);
    }

    private void saveSensorReads(ArrayList<SensorTransmissionCoder.SensorData> arrayMessage) {
        executor.execute(() -> {
            ArrayList<SensorMessageEntity> sensorMessageEntities = new ArrayList<>();
            for (SensorTransmissionCoder.SensorData s : arrayMessage) {
                sensorMessageEntities.add(s.toSensorDataMessage(LoginHelper.getUserCode(this)));
            }
            sensorMessageDao.insertAll(sensorMessageEntities);
        });
    }

    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(BackupService.this).sensorMessageDao();
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
        if (MqttHelper.connect()) {
            executor.execute(() -> {
                try {
                    final List<SensorMessageEntity> sensorsData = sensorMessageDao.getAll();

                    // Partition the sensors data in chunks and backup them to the remote database
                    if (sensorsData != null && !sensorsData.isEmpty()) {
                        // Necessary to avoid concurrent errors while iterating
                        final AtomicInteger counter = new AtomicInteger();
                        final int chunkSize = (int)(Math.ceil(sensorsData.size() / SensorableConstants.BACKUP_PART_SIZE));

                        sensorsData.stream()
                                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() % chunkSize))
                                .values()
                                .forEach(s -> backupSensorData(s));

                    } else {
                        Log.i("BACK UP SERVICE", "no rows to back up, see you soon");
                    }
                } catch (Exception e) {
                    Log.i("Sensors-back-up-service", "error getting the sensorMessageDao");
                }
            });
        } else {
            Log.e("BACK_UP_SERVICE", "No internet connection awaiting for it");
        }
    }

    // This method sends the passed sensors data to the mqtt broker to save it in the remote database
    // if the back service response to the proposed responseTopic then it was correctly done and it
    // proceeds the remove the sent data of the local database
    private void backupSensorData(final List<SensorMessageEntity> sensorsData) {
        // Generate the responseTopic to receive the response confirmation of data correctly saved in remote database
        final String responseTopic =
                SensorableConstants.MQTT_SENSORS_INSERT +
                        SensorableConstants.JSON_FIELDS_SEPARATOR +
                        LoginHelper.getUserCode(this) + System.currentTimeMillis();

        // Subscribe to the responseTopic delete users if its all correct and unsubscribe of it
        MqttHelper.subscribe(responseTopic, message -> {
            sensorMessageDao.deleteAll(sensorsData);
            MqttHelper.unsubscribe(responseTopic);
        });

        // Generate the string message and send
        String payload = "[" + sensorsData.stream().map(SensorMessageEntity::toJson).collect(Collectors.joining(",")) + "]";
        MqttHelper.publish(SensorableConstants.MQTT_SENSORS_INSERT, payload.getBytes(), responseTopic);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}