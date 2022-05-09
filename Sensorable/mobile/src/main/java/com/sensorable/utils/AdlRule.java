package com.sensorable.utils;

import android.content.Context;
import android.util.Log;

import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.BluetoothDeviceEntity;
import com.commons.database.DetectedAdlDao;
import com.commons.database.DetectedAdlEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AdlRule {
    private final String title;
    private final String description;
    private final SimpleDateFormat simpleDateFormat;
    private final Context context;
    private long firstTimestamp;
    private long lastTimestmap;
    private boolean previouslyDetected;
    private DetectedAdlDao detectedAdlDao;
    private BluetoothDeviceDao bluetoothDao;
    private ExecutorService executor;

    public AdlRule(Context context, String title, String description) {
        this.title = title;
        this.description = description;

        String pattern = "hh:mm dd-MM-yyyy";
        simpleDateFormat = new SimpleDateFormat(pattern);

        this.context = context;
        initializeMobileDatabase();
    }

    private void initializeMobileDatabase() {
        detectedAdlDao = MobileDatabaseBuilder.getDatabase(context).detectedAdlDao();
        bluetoothDao = MobileDatabaseBuilder.getDatabase(context).bluetoothDeviceDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public long getLastTimestmap() {
        return lastTimestmap;
    }

    public boolean wasPreviouslyDetected() {
        return previouslyDetected;
    }

    public boolean checkRule(boolean[] clausures) {
        // Evaluate the adl detection using the received params
        boolean adlRule = true;
        for (boolean clausure : clausures) {
            adlRule = adlRule & clausure;
        }

        // if the adl evaluation is positive then we track this adl state
        if (adlRule) {
            Log.i("ADL_DETECTION_SERVICE", "new adl detected -> " + title);

            // get starting time of this ADL
            if (!previouslyDetected) {
                firstTimestamp = getCurrentTimestamp();
            }

            // get always last time
            lastTimestmap = getCurrentTimestamp();
        }

        if (!adlRule && previouslyDetected) {
            // get detected devices by their timestamps, if we have a detection that
            // surrounds (its timestamps) the ADL, then we have to set to true the accompanied
            executor.execute(() -> {
                DetectedAdlEntity lastDetected = detectedAdlDao.getLastAdl(title, lastTimestmap - SensorableConstants.TIME_SINCE_LAST_ADL_DETECTION);

                if (lastDetected == null) {
                    List<BluetoothDeviceEntity> detectionsWhileAdl = bluetoothDao.getDevicesInRange(firstTimestamp, lastTimestmap);

                    detectedAdlDao.insert(
                            new DetectedAdlEntity(
                                    title,
                                    description,
                                    "start: " + firstTimestamp + " end: " + lastTimestmap,
                                    firstTimestamp,
                                    lastTimestmap,
                                    !detectionsWhileAdl.isEmpty()
                            )
                    );

                    Log.i("ADL_DETECTION_SERVICE", "inserted new detected adl");

                } else {
                    lastDetected.lastTimestamp = lastTimestmap;

                    String startDate = simpleDateFormat.format(new Date(lastDetected.firstTimestamp));
                    String endDate = simpleDateFormat.format(new Date(lastDetected.lastTimestamp));

                    lastDetected.stats = "start: " + startDate + " end: " + endDate;
                    detectedAdlDao.update(lastDetected);
                    Log.i("ADL_DETECTION_SERVICE", "updated timestamp");
                }
            });
        }


        // if the adl was detected then is true in other case is false
        previouslyDetected = adlRule;
        return adlRule;
    }

    private long getCurrentTimestamp() {
        return (new Date().getTime());
    }
}