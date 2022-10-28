package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.commons.SensorableConstants;
import com.commons.database.ActivityDao;
import com.commons.database.ActivityEntity;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.StepsForActivitiesDao;
import com.commons.database.StepsForActivitiesEntity;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.sensorable.utils.MobileDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class RegisterActivitiesService extends Service {

    private ActivityDao activityDao;
    private StepsForActivitiesDao stepsForActivitiesDao;
    private ActivityStepDao activityStepDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeMobileDatabase();
        loadDataViaMqtt();

        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");


        return super.onStartCommand(intent, flags, startId);
    }

    private void loadDataViaMqtt() {
        Log.i("MQTT_RECEIVE_ACTIVITIES", "before connection");

        if (MqttHelper.connect()) {
            final Consumer<Mqtt5Publish> handleReceivedActivities = mqtt5Publish -> {
                String payload = new String(mqtt5Publish.getPayloadAsBytes());
                String[] tables = payload.split(SensorableConstants.JSON_TABLES_SEPARATOR);

                updateFromDatabase(
                        composeTableActivities(removeFirstAndLastChar(tables[0])),
                        composeTableSteps(removeFirstAndLastChar(tables[1])),
                        composeTableStepsForActivities(removeFirstAndLastChar(tables[2]))
                );

                Log.i("MQTT_RECEIVE_ADLS", "new content " + payload);
            };

            MqttHelper.subscribe(SensorableConstants.MQTT_INFORM_ACTIVITIES, handleReceivedActivities);
            MqttHelper.publish(SensorableConstants.MQTT_REQUEST_CUSTOM_ADLS);
        }
    }

    private void updateFromDatabase(ArrayList<ActivityEntity> activities, ArrayList<ActivityStepEntity> steps, ArrayList<StepsForActivitiesEntity> stepsForActivities) {
        executor.execute(() -> {
            activityDao.deleteAll();
            activityStepDao.deleteAll();
            stepsForActivitiesDao.deleteAll();

            activityDao.insertAll(activities);
            activityStepDao.insertAll(steps);
            stepsForActivitiesDao.insertAll(stepsForActivities);
        });

    }

    private ArrayList<ActivityEntity> composeTableActivities(final String activities) {
        return new ArrayList<>();
    }

    private ArrayList<ActivityStepEntity> composeTableSteps(final String steps) {
        return new ArrayList<>();
    }

    private ArrayList<StepsForActivitiesEntity> composeTableStepsForActivities(final String stepsForActivities) {
        return new ArrayList<>();
    }


    private String removeFirstAndLastChar(String someString) {
        return someString.substring(1, someString.length() - 1);
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        MobileDatabase database = MobileDatabaseBuilder.getDatabase(this);

        activityDao = database.activityDao();
        activityStepDao = database.activityStepDao();
        stepsForActivitiesDao = database.stepsForActivitiesDao();

        executor = MobileDatabaseBuilder.getExecutor();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}