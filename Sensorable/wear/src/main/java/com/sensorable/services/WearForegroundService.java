package com.sensorable.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.commons.utils.Actions;
import com.commons.utils.CsvSaver;
import com.commons.utils.LoginHelper;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableDatabase;
import com.commons.utils.SensorsProvider;
import com.commons.utils.ServiceState;
import com.commons.utils.ServiceStatePreferences;
import com.sensorable.R;
import com.sensorable.utils.WearDatabaseBuilder;
import com.sensorable.utils.WearSensorDataSender;
import com.sensorable.utils.WearosEnvironment;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public class WearForegroundService extends Service {
    private static final int[] listenedSensors = {
            Sensor.TYPE_HEART_RATE,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE
    };

    private final PowerManager.WakeLock wakeLock = null;
    private final String TAG = "WearForegroundService::lock";

    private final ArrayList<SensorTransmissionCoder.SensorData> sensorDataBuffer = new ArrayList<>();
    private final ArrayList<SensorTransmissionCoder.SensorData> csvSensorsBuffer = new ArrayList<>();

    private boolean isServiceStarted = false;
    private SensorableDatabase database;
    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;

    @Override
    public void onCreate() {
        super.onCreate();

        Notification notification = createNotification();
        startForeground(1, notification);

        initializeDatabase();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(Actions.START.name())) {
                startService();
            } else if (action.equals(Actions.STOP.name())) {
                stopService();
            } else {
                Log.d("SERVICE", "This should never happen. No action in the received intent");
            }

        } else {
            Log.d("SERVICE",
                    "with a null intent. It has been probably restarted by the system."
            );
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;

    }

    private void doForegroundJob() {
        // function to do as a foregound service
        Log.d(TAG, "Working properly");

        SensorsProvider sensorsProvider = new SensorsProvider(this);
        WearSensorDataSender sensorSender = new WearSensorDataSender(this);

        SensorEventListener listenerDataSender = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                SensorTransmissionCoder.SensorData newSensorEvent =
                        new SensorTransmissionCoder.SensorData(
                                WearosEnvironment.getDeviceType(),
                                sensorEvent.sensor.getType(),
                                sensorEvent.values
                        );

                sensorSender.sendMessage(newSensorEvent);

                exportData(newSensorEvent);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };


        for (int sensorCode : listenedSensors) {
            sensorsProvider.subscribeToSensor(sensorCode, listenerDataSender, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // method to exportData, it receives sensor reads received from the sensorsProvider
    private void exportData(SensorTransmissionCoder.SensorData newSensorEvent) {
        sensorDataBuffer.add(newSensorEvent);

        // if we have the desired amount to save or even more
        if (sensorDataBuffer.size() >= SensorableConstants.MAX_COLLECTED_DATA_EXPORT_CSV) {
            ArrayList<SensorTransmissionCoder.SensorData> newSensors = new ArrayList<>(sensorDataBuffer);

            // remove all the saved sensors from the buffer
            sensorDataBuffer.clear();
            ArrayList<SensorMessageEntity> sensorEntities = SensorTransmissionCoder.SensorData.toSensorDataMessages(newSensors);

            // save into the local database format
            executor.execute(() -> sensorMessageDao.insertAll(sensorEntities));

            // save into the csv file
            CsvSaver.exportToCsv(sensorEntities, Objects.toString(LoginHelper.getUserCode(getApplicationContext()), "NULL"));
        }
    }

    private void initializeDatabase() {
        database = WearDatabaseBuilder.getDatabase(this);
        sensorMessageDao = database.sensorMessageDao();
        executor = WearDatabaseBuilder.getExecutor();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {

        String notificationChannelId = "ENDLESS SERVICE CHANNEL";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Endless Service channel");
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        notificationManager.createNotificationChannel(channel);

        // Notification action to stop the service (a button will appear on the device)
        Intent stopServiceIntent = new Intent(this, WearForegroundService.class);
        stopServiceIntent.setAction(Actions.STOP.name());

        PendingIntent stopIntent = PendingIntent.getService(this, 0, stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.layout.stop_24, "Parar", stopIntent).build();

        return builder
                .setContentTitle("CSV saver")
                .setContentText("Guardando lecturas en CSV")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("CSV")
                .addAction(action)
                .build();

    }

    private void startService() {

        if (isServiceStarted) return;
        isServiceStarted = true;
        ServiceStatePreferences.setServiceState(this, ServiceState.STARTED); // Set the service state to started

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire(); // we need this lock so our service gets not affected by Doze Mode

        // Start the sensors and do the needed function
        doForegroundJob();

    }


    private void stopService() {
        Log.d("SERVICE", "Se ha parado el servicio");

        // we need this lock so our service gets not affected by Doze Mode
        try {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
            stopSelf();
        } catch (Exception e) {
            Log.d("SERVICE", "Service stopped without being started: " + e.getMessage());
        }

        // Set the service to stopped
        isServiceStarted = false;
        isServiceStarted = false;
        ServiceStatePreferences.setServiceState(this, ServiceState.STOPPED);
    }

}