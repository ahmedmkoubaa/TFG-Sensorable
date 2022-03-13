package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.database.DetectedAdlDao;
import com.commons.database.DetectedAdlEntity;
import com.sensorable.utils.MobileDatabaseBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AdlDetectionService extends Service {
    
    private BroadcastReceiver mobileReceiver;
    private boolean CLOSE_PROXIMITY = false;

    private DetectedAdlDao detectedAdlDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "ADL DETECTION SERVICE", Toast.LENGTH_SHORT).show();

        initializeMobileDatabase();
        initializeMobileReciver();

        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");


        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeMobileDatabase() {
        detectedAdlDao = MobileDatabaseBuilder.getDatabase(this).detectedAdlDao();
        executor = MobileDatabaseBuilder.getExecutor();
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("AdlUpdates");
        // You can also include some extra data.

        Bundle empaticaBundle = new Bundle();
        empaticaBundle.putString("AdlMessage", msg);

        intent.putExtra("ADL_DATA_COLLECTED", empaticaBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeMobileReciver() {
        mobileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra("MOBILE_DATA_COLLECTED");
                ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList("MobileMessage");
                detectAdls(arrayMessage);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mobileReceiver, new IntentFilter("MOBILE_SENDS_SENSOR_DATA"));
    }

    private void detectAdls(ArrayList<SensorTransmissionCoder.SensorMessage> data) {
        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData = filterData(data);
        searchPatterns(filteredData);
    }

    private void searchPatterns(HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData) {
        boolean COUNTING_STEPS, LOW_LIGHT, VERTICAL_PHONE;

        AdlRule phoneCall = new AdlRule(
                "Have a phone call",
                "System detected a phone call sicne first timestamp to last tiemstamp. System has monitorized your body and phone position"
        );

        // For each data stage of time, we are going to process the sensor reading
        for (long key : filteredData.keySet()) {

            LOW_LIGHT = VERTICAL_PHONE = COUNTING_STEPS = false;

            // Now, we look into the sensor readings and use sensor that we want to
            // generate conditions and later shoot rules
            for (SensorTransmissionCoder.SensorMessage s : filteredData.get(key)) {

                // evaluate conditions if we have any sensor
                switch (s.getDeviceType()) {
                    case DeviceType.MOBILE:

                        switch (s.getSensorType()) {
                            case Sensor.TYPE_HEART_RATE:
                                break;
                            case Sensor.TYPE_PROXIMITY:
                                CLOSE_PROXIMITY = s.getValue()[0] == 0;
                                Log.i("ADL_DETECTION_SERVICE", "PROXIMITY " + s.getValue()[0]);
                                break;

                            case Sensor.TYPE_LIGHT:
                                LOW_LIGHT = s.getValue()[0] <= 15;
                                Log.i("ADL_DETECTION_SERVICE", "LIGHT " + s.getValue()[0]);
                                break;

                            case Sensor.TYPE_ACCELEROMETER:
                                VERTICAL_PHONE = -4 <= s.getValue()[2] && s.getValue()[2] <= 4;
                                Log.i("ADL_DETECTION_SERVICE", "ACCELEROMETER " + s.getValue()[2]);
                                break;

                            case Sensor.TYPE_STEP_COUNTER:
                                COUNTING_STEPS = true;
                                Log.i("ADL_DETECTION_SERVICE", "STEP_COUNTER " + s.getValue()[0]);
                                break;
                        }
                        break;
                }


                // rules to shoot
                boolean[] values = {LOW_LIGHT, CLOSE_PROXIMITY, VERTICAL_PHONE};
                phoneCall.checkRule(values);

                detectWalking(COUNTING_STEPS);
            }
        }
    }

    private void detectWalking(boolean countingSteps) {
        if (countingSteps) {
            Log.i("NEWADL", "Detected ADL walking");
        }
    }

    private HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filterData(ArrayList<SensorTransmissionCoder.SensorMessage> data) {
        long floorTimestamp;

        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> categorization =
                new HashMap<>();

        // iterate data sensor to categorize each sensor read per timestamp
        for (SensorTransmissionCoder.SensorMessage newSensorMessage : data) {
            floorTimestamp = newSensorMessage.getTimestamp() / SensorableConstants.ADL_FILTER_TIME;


            // if the categorization for these timestamp exists
            if (categorization.containsKey(floorTimestamp)) {
                ArrayList<SensorTransmissionCoder.SensorMessage>
                        arrayByTimestamp = categorization.get(floorTimestamp);


                boolean found = false;
                ArrayList<SensorTransmissionCoder.SensorMessage> copyArray = new ArrayList<>(arrayByTimestamp);


                // We want only the last sensor read from each categorization per timestamp
                for (SensorTransmissionCoder.SensorMessage oldSensorMessage : arrayByTimestamp) {
                    if (oldSensorMessage.getDeviceType() == newSensorMessage.getDeviceType()
                            && oldSensorMessage.getSensorType() == newSensorMessage.getSensorType()) {

                        // if the current value of this sensor from this device is older than the new
                        // then we replace it removing the old and inserting the new
                        if (oldSensorMessage.getTimestamp() < newSensorMessage.getTimestamp()) {
                        /*    arrayByTimestamp.remove(oldSensorMessage);
                            arrayByTimestamp.add(newSensorMessage);*/

                            copyArray.remove(oldSensorMessage);
                            copyArray.add(newSensorMessage);

                            found = true;
                        }
                    }
                }

                if (!found) {
                    copyArray.add(newSensorMessage);

                }

                categorization.replace(floorTimestamp, copyArray);

            } else {
                ArrayList<SensorTransmissionCoder.SensorMessage> newArray = new ArrayList<>();
                newArray.add(newSensorMessage);
                categorization.put(floorTimestamp, newArray);
            }
        }


        for (long key : categorization.keySet()) {
            for (SensorTransmissionCoder.SensorMessage s : categorization.get(key)) {

                Log.i("ADL_DETECTION_SERVICE", " filtered data -> " + s.toString());
            }
        }

        return categorization;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class AdlRule {
        private final String title;
        private final String description;
        private long firstTimestamp;
        private long lastTimestmap;
        private boolean previouslyDetected;

        public AdlRule(String title, String description) {
            this.title = title;
            this.description = description;
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
                Log.i("ADL_DETECTION_SERVICE", "new adl detected -> PHONE-CALL-DETECTED");

                // get starting time of this ADL
                if (!previouslyDetected) {
                    firstTimestamp = getCurrentTimestamp();
                }

                // get always last time
                lastTimestmap = getCurrentTimestamp();
            }

            if (!adlRule && previouslyDetected) {
                executor.execute(() -> {
                    detectedAdlDao.insert(
                            new DetectedAdlEntity(
                                    title,
                                    description,
                                    "start: " + firstTimestamp + " end: " + lastTimestmap,
                                    firstTimestamp)
                    );

                    Log.i("ADL_DETECTION_SERVICE", "inserted new adl in database");
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
}