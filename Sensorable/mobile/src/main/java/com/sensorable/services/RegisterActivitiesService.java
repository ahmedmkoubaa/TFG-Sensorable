package com.sensorable.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.commons.utils.SensorableConstants;
import com.commons.database.ActivityDao;
import com.commons.database.ActivityEntity;
import com.commons.database.ActivityStepDao;
import com.commons.database.ActivityStepEntity;
import com.commons.database.StepsForActivitiesDao;
import com.commons.database.StepsForActivitiesEntity;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.commons.utils.SensorableDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;
import com.sensorable.utils.TablesFormatter;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            final Consumer<Mqtt5Publish> handleReceivedActivities = payload -> {
                String[] tables = TablesFormatter.getTables(payload);

                try {
                    updateActivityRegistries(
                            TablesFormatter.composeTableActivities(tables[0]),
                            TablesFormatter.composeTableSteps(tables[1]),
                            TablesFormatter.composeTableStepsForActivities(tables[2])
                    );
                } catch (NullPointerException e) {
                    Log.e("REGISTER ACTIVITIES", e.getMessage());
                }

                Log.i("REGISTER ACTIVITIES", "Received new activities to register");
            };

            MqttHelper.subscribe(SensorableConstants.MQTT_INFORM_ACTIVITIES, handleReceivedActivities);
            MqttHelper.publish(SensorableConstants.MQTT_REQUEST_ACTIVITIES);
        }
    }

    //This method updates the activities registry table entities by updating their stored
    // data and removing the non sent (from remote db) data.
    private void updateActivityRegistries(final ArrayList<ActivityEntity> activitiesEntities,
                                          final ArrayList<ActivityStepEntity> stepsEntities,
                                          final ArrayList<StepsForActivitiesEntity> stepsForActivityEntities) {
        executor.execute(() -> {
            updateActivities(activitiesEntities);
            updateSteps(stepsEntities);
            updateStepsForActivities(stepsForActivityEntities);
        });
    }

    private void updateActivities(final ArrayList<ActivityEntity> activitiesEntities) {
        // update activity list
        if ((activityDao.size() > 0)) {
            activityDao.update(activitiesEntities);
        } else {
            activityDao.insertAll(activitiesEntities);
        }

        activityDao.delete(
                activityDao.getAll()
                        .stream()
                        .filter(activityEntity -> !activitiesEntities.contains(activityEntity))
                        .collect(Collectors.toList())
        );
    }

    private void updateSteps(final ArrayList<ActivityStepEntity> stepsEntities) {
        // update steps list
        if (activityStepDao.size() > 0) {
            activityStepDao.update(stepsEntities);
        } else {
            activityStepDao.insertAll(stepsEntities);
        }

        activityStepDao.delete(
                activityStepDao.getAll()
                        .stream()
                        .filter(activityStepEntity -> !stepsEntities.contains(activityStepEntity))
                        .collect(Collectors.toList())
        );
    }

    private void updateStepsForActivities(final ArrayList<StepsForActivitiesEntity> stepsForActivityEntities) {
        // update stepsForActivities list
        if (stepsForActivitiesDao.size() > 0) {
            stepsForActivitiesDao.update(stepsForActivityEntities);
        } else {
            stepsForActivitiesDao.insertAll(stepsForActivityEntities);
        }
        stepsForActivitiesDao.delete(
                stepsForActivitiesDao.getAll()
                        .stream()
                        .filter(stepForAct -> !stepsForActivityEntities.contains(stepForAct))
                        .collect(Collectors.toList())
        );
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        SensorableDatabase database = MobileDatabaseBuilder.getDatabase(this);

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