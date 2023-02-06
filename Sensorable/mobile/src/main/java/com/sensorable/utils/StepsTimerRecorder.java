package com.sensorable.utils;

import android.util.Pair;

import com.commons.database.StepsForActivitiesRegistryEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class StepsTimerRecorder {
    // These are reserved ids
    private static final int startId = -1;
    private static final int stopId = -2;
    private static final MobileDatabase database = MobileDatabaseBuilder.getDatabase(null);
    private static final ExecutorService executor = MobileDatabaseBuilder.getExecutor();

    public static void startRecordingSteps(final long activityId, final String userCode) {
        saveTag(activityId, startId, userCode);
    }

    public static void stopRecordingSteps(final long activityId, final String userCode) {
        saveTag(activityId, stopId, userCode);
    }

    public static void saveTag(long activityId, int stepId, final String userCode) {
        long timestamp = new Date().getTime();

        // get the id of the relation between activity and step and save it into
        executor.execute(() ->
                {
                    database.stepsForActivitiesRegistryDao().insert(
                            new StepsForActivitiesRegistryEntity(
                                    activityId, stepId,timestamp, userCode
                            )
                    );
                }
        );
    }
}
