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
import com.sensorable.utils.TablesFormatter;
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
            final Consumer<Mqtt5Publish> handleReceivedActivities = payload -> {
                String[] tables = TablesFormatter.getTables(payload);

                try {
                    updateActivityRegistries(
                            TablesFormatter.composeTableActivities(tables[0]),
                            TablesFormatter.composeTableSteps(tables[1]),
                            TablesFormatter.composeTableStepsForActivities(tables[2])
                    );
                } catch(NullPointerException e) {
                    Log.e("REGISTER ACTIVITIES", e.getMessage());
                }

                Log.i("REGISTER ACTIVITIES", "Received new activities to register");
            };

            MqttHelper.subscribe(SensorableConstants.MQTT_INFORM_ACTIVITIES, handleReceivedActivities);
            MqttHelper.publish(SensorableConstants.MQTT_REQUEST_ACTIVITIES);
        }
    }

    // Remove the previous adls scheme in database and save the new in order to have
    // the new version received from the remote DB
    private void updateActivityRegistries(final ArrayList<ActivityEntity> activitiesEntities,
                                          final ArrayList<ActivityStepEntity> stepsEntities,
                                          final ArrayList<StepsForActivitiesEntity> stepsForactivityEntities) {
        executor.execute(() -> {
            activityDao.deleteAll();
            activityStepDao.deleteAll();
            stepsForActivitiesDao.deleteAll();

            activityDao.insertAll(activitiesEntities);
            activityStepDao.insertAll(stepsEntities);
            stepsForActivitiesDao.insertAll(stepsForactivityEntities);
        });
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