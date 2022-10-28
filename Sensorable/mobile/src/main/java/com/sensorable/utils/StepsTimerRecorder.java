
package com.sensorable.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;

public class StepsTimerRecorder {
    // These are reserved ids
    private static final int startId = -1;
    private static final int stopId = -2;

    // sequence of stored steps
    private static final ArrayList<Pair<Integer, Long>> stepsSequence = new ArrayList<>();

    public static void startRecordinSteps() {
        saveTag(startId);
    }

    public static void stopRecordingSteps() {
        saveTag(stopId);
    }

    public static void saveTag(int stepId) {
        stepsSequence.add(setTag(stepId));
    }

    public static Pair<Integer, Long> setTag(int stepId) {
        return new Pair<>(stepId, new Date().getTime());
    }

}
