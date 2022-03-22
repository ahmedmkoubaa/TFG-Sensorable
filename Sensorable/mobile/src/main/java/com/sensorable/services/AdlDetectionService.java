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

import com.commons.AdlRule;
import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.database.BluetoothDeviceDao;
import com.commons.database.DetectedAdlDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AdlDetectionService extends Service {
    private boolean CLOSE_PROXIMITY = false;

    private DetectedAdlDao detectedAdlDao;
    private ExecutorService executor;
    private BluetoothDeviceDao bluetoothDao;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "ADL DETECTION SERVICE", Toast.LENGTH_SHORT).show();
        initializeMobileReciver();
        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");

        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent(SensorableConstants.ADL_UPDATE);

        Bundle empaticaBundle = new Bundle();
        empaticaBundle.putString(SensorableConstants.BROADCAST_MESSAGE, msg);

        intent.putExtra(SensorableConstants.EXTRA_MESSAGE, empaticaBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeMobileReciver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        detectAdls(arrayMessage);
                    }
                }, new IntentFilter(SensorableConstants.MOBILE_SENDS_SENSOR_DATA));
    }

    private void detectAdls(ArrayList<SensorTransmissionCoder.SensorMessage> data) {
        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData = filterData(data);
        searchPatterns(filteredData);
    }

    private void searchPatterns(HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData) {
        boolean COUNTING_STEPS, LOW_LIGHT, VERTICAL_PHONE;

        AdlRule phoneCall = new AdlRule(
                this,
                "Tuviste una llamada de teléfono",
                "El sistema detectó una llamada teléfonica. El sistema se basó en tu postura corporal y en la del teléfono"
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

                        }
                        break;
                }

                // rules to shoot
                boolean[] values = {LOW_LIGHT, CLOSE_PROXIMITY, VERTICAL_PHONE};
                phoneCall.checkRule(values);
            }
        }
    }

    private HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filterData(ArrayList<SensorTransmissionCoder.SensorMessage> data) {
        long floorTimestamp;

        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>>
                categorization = new HashMap<>();

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

                    // we want to know if we have in the same stage (categorization) a repeated sensor data
                    if (oldSensorMessage.getDeviceType() == newSensorMessage.getDeviceType()
                            && oldSensorMessage.getSensorType() == newSensorMessage.getSensorType()) {

                        // if the current value of this sensor from this device is older than the new
                        // then we replace it removing the old and inserting the new
                        if (oldSensorMessage.getTimestamp() < newSensorMessage.getTimestamp()) {
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

/*      TODO remove this comment once we are confident about its usage
        for (long key : categorization.keySet()) {
            for (SensorTransmissionCoder.SensorMessage s : categorization.get(key)) {
                Log.i("ADL_DETECTION_SERVICE", " filtered data -> " + s.toString());
            }
        }*/

        return categorization;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}