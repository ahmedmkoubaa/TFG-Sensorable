package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.commons.SensorableConstants;
import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class BackUpService extends Service {

    private SensorMessageDao sensorMessageDao;
    private ExecutorService executorService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BACKUP_SERVICE", "started backup service correctly");

        initializeMobileDatabase();
        initializeReminders();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMobileDatabase() {
        sensorMessageDao = MobileDatabaseBuilder.getDatabase(BackUpService.this).sensorMessageDao();
        executorService = MobileDatabaseBuilder.getExecutor();
    }


    private void initializeReminders() {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // do something
                MqttHelper.connect();

                executorService.execute(() -> {
                    List<SensorMessageEntity> sensorsData = null;

                    try {
                        sensorsData = sensorMessageDao.getAll();
                    } catch (Exception e) {
                        Log.i("Sensors-back-up-service", "error getting the sensorMessageDao");
                    }

                    if (sensorsData != null && !sensorsData.isEmpty()) {
                        final double parts = Math.ceil(sensorsData.size() / SensorableConstants.BACKUP_PART_SIZE);

                        int i = 0;
                        for (int j = 1; j <= parts; j++) {
                            String payload = "[ ";

                            for (i = (int) ((j - 1) * parts); i < sensorsData.size() && i < j * SensorableConstants.BACKUP_PART_SIZE; i++) {
                                payload += sensorsData.get(i).toJson() + ",";
                            }

                            payload = payload.substring(0, payload.length() - 1) + "]";

                            MqttHelper.publish(SensorableConstants.MQTT_SENSORS_INSERT, payload.getBytes());
                            Log.i("TEST_MQTT", payload);
                        }

                        sensorMessageDao.deleteAll();
                    } else {
                        Log.i("ALARM_RECEIVER", "no rows to back up, see you soon");
                    }

                });


                Log.i("BACKUP_SERVICE", "ALARM FIRING AGAIN");
                handler.postDelayed(this, SensorableConstants.SCHEDULE_DATABASE_BACKUP);
            }
        };

        handler.post(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}