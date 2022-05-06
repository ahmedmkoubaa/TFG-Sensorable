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

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.DeviceType;
import com.commons.SensorTransmissionCoder;
import com.commons.SensorableConstants;
import com.commons.database.AdlDao;
import com.commons.database.AdlEntity;
import com.commons.database.AdlRegistryDao;
import com.commons.database.AdlRegistryEntity;
import com.commons.database.EventDao;
import com.commons.database.EventEntity;
import com.commons.database.EventForAdlDao;
import com.commons.database.EventForAdlEntity;
import com.commons.database.KnownLocationDao;
import com.commons.database.KnownLocationEntity;
import com.sensorable.utils.AdlRule;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.SensorAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class AdlDetectionService extends Service {
    private final ArrayList<EventEntity> events = new ArrayList<>();
    private final ArrayList<EventForAdlEntity> eventsForAdls = new ArrayList<>();
    private final ArrayList<AdlEntity> adls = new ArrayList<>();
    private final ArrayList<KnownLocationEntity> knownLocations = new ArrayList<>();
    private final HashMap<Integer, ArrayList<Pair<Integer, Boolean>>> databaseAdls = new HashMap<>();

    private EventDao eventDao;
    private AdlDao adlDao;
    private EventForAdlDao eventForAdlDao;
    private KnownLocationDao knownLocationDao;
    private AdlRegistryDao adlRegistryDao;
    private ExecutorService executor;

    private boolean CLOSE_PROXIMITY = false;

    private static boolean equal(float leftOperand, float rightOperand) {
        return leftOperand == rightOperand;
    }

    private static boolean notEqual(float leftOperand, float rightOperand) {
        return leftOperand != rightOperand;
    }

    private static boolean greaterEqual(float leftOperand, float rightOperand) {
        return leftOperand >= rightOperand;
    }

    private static boolean lessEqual(float leftOperand, float rightOperand) {
        return leftOperand <= rightOperand;
    }

    private static boolean greater(float leftOperand, float rightOperand) {
        return leftOperand > rightOperand;
    }

    private static boolean less(float leftOperand, float rightOperand) {
        return leftOperand < rightOperand;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeMobileReceiver();
        initializeMobileDatabase();

        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");

        return super.onStartCommand(intent, flags, startId);
    }

    // initialize data structures from the database
    private void initializeMobileDatabase() {
        eventDao = MobileDatabaseBuilder.getDatabase(this).eventDao();
        eventForAdlDao = MobileDatabaseBuilder.getDatabase(this).eventForAdlDao();
        adlDao = MobileDatabaseBuilder.getDatabase(this).adlDao();

        knownLocationDao = MobileDatabaseBuilder.getDatabase(this).knownLocationDao();
        adlRegistryDao = MobileDatabaseBuilder.getDatabase(this).adlRegistryDao();


        executor = MobileDatabaseBuilder.getExecutor();
        executor.execute(() -> {
            events.addAll(eventDao.getAll());
            adls.addAll(adlDao.getAll());
            eventsForAdls.addAll(eventForAdlDao.getAll());

            // generation of data structure to evaluate adls
            adls.forEach(adlEntity -> {
                ArrayList<Pair<Integer, Boolean>> eventsOfCurrentAdl = new ArrayList<>();
                eventsForAdls.forEach(eventForAdlEntity -> {
                    if (eventForAdlEntity.idAdl == adlEntity.id) {
                        eventsOfCurrentAdl.add(new Pair<>(eventForAdlEntity.idEvent, false));
                    }
                });

                databaseAdls.put(adlEntity.id, eventsOfCurrentAdl);
            });

            knownLocations.addAll(knownLocationDao.getAll());
        });
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent(SensorableConstants.ADL_UPDATE);

        Bundle empaticaBundle = new Bundle();
        empaticaBundle.putString(SensorableConstants.BROADCAST_MESSAGE, msg);

        intent.putExtra(SensorableConstants.EXTRA_MESSAGE, empaticaBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initializeMobileReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                        ArrayList<SensorTransmissionCoder.SensorMessage> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                        detectAdls(arrayMessage);

                        Log.i("ADL_DETECTION_SERVICE", "received new data from mobile " + arrayMessage.size());
                    }
                }, new IntentFilter(SensorableConstants.MOBILE_SENDS_SENSOR_DATA));
    }

    private void detectAdls(ArrayList<SensorTransmissionCoder.SensorMessage> data) {
        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData = filterData(data);
        searchPatterns(filteredData);
        searchPatternsExtended(filteredData);
    }

    private HashMap<Integer, Boolean> evaluateEvents(HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData) {
        HashMap<Integer, Boolean> evaluatedEvents = new HashMap<>();
        SensorOperation operation;

        for (EventEntity e : events) {
            for (long timestamp : filteredData.keySet()) {

                // Now, we look into the sensor readings and use sensor that we want to
                // generate conditions and later shoot rules
                for (SensorTransmissionCoder.SensorMessage s : filteredData.get(timestamp)) {

                    // evaluate the events
                    if (s.getDeviceType() == e.deviceType && s.getSensorType() == e.sensorType) {
                        switch (e.operator) {
                            case EQUAL:
                                operation = AdlDetectionService::equal;
                                break;

                            case NOT_EQUAL:
                                operation = AdlDetectionService::notEqual;
                                break;

                            case LESS:
                                operation = AdlDetectionService::less;
                                break;

                            case LESS_EQUAL:
                                operation = AdlDetectionService::lessEqual;
                                break;

                            case GREATER:
                                operation = AdlDetectionService::greater;
                                break;

                            case GREATER_EQUAL:
                                operation = AdlDetectionService::greaterEqual;
                                break;

                            default:
                                operation = null;
                                Log.i("ADL_DETECTION_SERVICE", "not recognized operand, something went wrong");
                        }

                        boolean evaluation = false;

                        switch (e.pos) {
                            case SensorAction.FIRST:
                            case SensorAction.SECOND:
                            case SensorAction.THIRD:

                                evaluation = operation.operate(s.getValue()[e.pos], e.operand);
                                break;

                            case SensorAction.DISTANCE:
                                // Let's look for the desired location
                                for (KnownLocationEntity k : knownLocations) {
                                    if (k.tag.equals(e.tag)) {

                                        // calculate 3d distance from current gps value (in s) and the known location whose tag fits
                                        float distance = (float) Math.sqrt(
                                                Math.pow(s.getValue()[0] - k.altitude, 2) +
                                                        Math.pow(s.getValue()[1] - k.latitude, 2) +
                                                        Math.pow(s.getValue()[2] - k.longitude, 2)
                                        );

                                        if (evaluation = operation.operate(distance, e.operand)) {
                                            break;
                                        }
                                    }
                                }

                                break;
                            case SensorAction.ANY:
                                evaluation = operation.operate(s.getValue()[0], e.operand) ||
                                        operation.operate(s.getValue()[1], e.operand) ||
                                        operation.operate(s.getValue()[2], e.operand);
                                break;

                            case SensorAction.ALL:
                                evaluation = operation.operate(s.getValue()[0], e.operand) &&
                                        operation.operate(s.getValue()[1], e.operand) &&
                                        operation.operate(s.getValue()[2], e.operand);

                        }

                        if (operation != null) {
                            evaluatedEvents.put(e.id, evaluation);
                        } else {
                            Log.i("ADL_DETECTION_SERVICE", "null operation, operator bad specified");
                        }
                    }
                }
            }
        }

        return evaluatedEvents;
    }

    private void searchPatternsExtended(HashMap<Long, ArrayList<SensorTransmissionCoder.SensorMessage>> filteredData) {
        HashMap<Integer, Boolean> evaluatedEvents = evaluateEvents(filteredData);

        // let's take from database adls the events registry owned by each adl
        for (int idCurrentAdl : databaseAdls.keySet()) {
            ArrayList<Pair<Integer, Boolean>> eventsOfCurrentAdl = databaseAdls.get(idCurrentAdl);
            int size = eventsOfCurrentAdl.size();

            for (int i = 0; i < size; i++) {
                // check if previous adl in the sorted events array occured, if happened
                // then we check the next event, the current and update if necessary
                Pair<Integer, Boolean> event = eventsOfCurrentAdl.get(i);
                if (!event.second) {

                    if ((i == 0 || (i > 0 && eventsOfCurrentAdl.get(i - 1).second) || size == 1) &&
                            evaluatedEvents.containsKey(event.first) && evaluatedEvents.get(event.first)) {

                        eventsOfCurrentAdl.set(i, new Pair<>(event.first, true));

                    } else {
                        // If the value of the current event is not true
                        // then we don't need to check the next values because
                        // they have to be true in the specified order.
                        break;
                    }
                }
            }

            boolean evaluation = true;
            for (Pair<Integer, Boolean> event : eventsOfCurrentAdl) {
                evaluation &= event.second;
                if (!evaluation) {
                    break;
                }
            }

            if (evaluation) {
                Log.i("ADL_DETECTION_SERVICE", "recognized a new adl");

                executor.execute(() -> {
                    long currentTime = new Date().getTime();

                    // interval is the current time less 5 minutes, counts made on millis
                    long sinceTime = currentTime - SensorableConstants.TIME_SINCE_LAST_ADL_DETECTION;
                    AdlRegistryEntity res = adlRegistryDao.getAdlRegistryAfter(sinceTime);

                    if (res != null) {
                        res.endTime = currentTime;
                        adlRegistryDao.update(res);
                    } else {
                        adlRegistryDao.insert(
                                new AdlRegistryEntity(idCurrentAdl, currentTime, currentTime)
                        );
                    }
                });

                // check if the adl stills being evaluated (all its events)
                boolean previousFalse = false;
                for (int i = 0; i < size; i++) {
                    // check if previous adl in the sorted events array occured, if happened
                    // then we check the next event, the current and update if necessary
                    Pair<Integer, Boolean> event = eventsOfCurrentAdl.get(i);

                    if (previousFalse || (event.second && evaluatedEvents.containsKey(event.first) && !evaluatedEvents.get(event.first))) {
                        eventsOfCurrentAdl.set(i, new Pair<>(event.first, false));
                        previousFalse = true;
                    }
                }

                // get the last database stored in the adl in the last stage of time
                // if you get one we update the finish timestamp
                // else you add a new row to the database
            } else {
                Log.i("ADL_DETECTION_SERVICE", "no longer recognized an old adl");

            }
        }

        Log.i("ADL_DETECION_SERVICE", evaluatedEvents + " ");
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

    @FunctionalInterface
    private interface SensorOperation {
        boolean operate(float leftOperand, float rightOperand);
    }


}