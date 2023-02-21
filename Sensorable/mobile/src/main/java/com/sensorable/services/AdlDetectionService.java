package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableIntentFilters;
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
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.sensorable.utils.TablesFormatter;
import com.commons.utils.SensorableDatabase;
import com.sensorable.utils.MobileDatabaseBuilder;
import com.sensorable.utils.MqttHelper;
import com.sensorable.utils.SensorOperations;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class AdlDetectionService extends Service {
    private final ArrayList<SensorTransmissionCoder.SensorData> sensorDataBuffer = new ArrayList<>();
    private final ArrayList<EventEntity> events = new ArrayList<>();
    private final ArrayList<EventForAdlEntity> eventsForAdls = new ArrayList<>();
    private final ArrayList<AdlEntity> adls = new ArrayList<>();
    private final ArrayList<KnownLocationEntity> knownLocations = new ArrayList<>();
    private final HashMap<Integer, HashMap<Integer, ArrayList<Pair<Integer, Boolean>>>> databaseAdls = new HashMap<>();
    private BroadcastReceiver dataReceiver;
    private EventDao eventDao;
    private AdlDao adlDao;
    private EventForAdlDao eventForAdlDao;
    private KnownLocationDao knownLocationDao;
    private AdlRegistryDao adlRegistryDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeDataReceiver();
        initializeMobileDatabase();
        initializeMqttClient();

        Log.i("ADL_DETECTION_SERVICE", "initialized adl detection service");


        return super.onStartCommand(intent, flags, startId);
    }

    // Subscribe to MQTT broker and request the ADLs scheme
    private void initializeMqttClient() {
        Log.i("MQTT_RECEIVE_ADLS", "before connection");

        // Each time we initialize this service is necessary to retrieve data from the remote database
        // so we establish a connection via MQTT, if this connection doesn't work properly, then we use
        // the previous stored data
        if (MqttHelper.connect()) {
            final Consumer<Mqtt5Publish> handleAdlsScheme = payload -> {
                String[] tables = TablesFormatter.getTables(payload);

                try {
                    updateADLsScheme(
                            TablesFormatter.composeTableAdls((tables[0])),
                            TablesFormatter.composeTableEvents((tables[1])),
                            TablesFormatter.composeTableEventsForAdls((tables[2]))
                    );
                } catch (NullPointerException e) {
                    Log.e("MQTT RECEIVE ADLS", e.getMessage());
                }

                Log.i("MQTT_RECEIVE_ADLS", "new adls scheme received");
            };

            // ask for new adls scheme
            String session = null;

            // If there is a current session, then we'll communicate via our own response topic
            // in order to get our custom ADLs, in other case we'll subscribe to the generic adls topic
            if (session != null) {
                String responseTopic = SensorableConstants.MQQTT_INFORM_CUSTOM_ADLS + "/" + session;

                MqttHelper.subscribe(responseTopic, handleAdlsScheme);
                MqttHelper.publish(
                        SensorableConstants.MQTT_REQUEST_CUSTOM_ADLS,
                        session.getBytes(),
                        responseTopic
                );
            } else {
                MqttHelper.subscribe(SensorableConstants.MQTT_INFORM_GENERIC_ADLS, handleAdlsScheme);
                MqttHelper.publish(SensorableConstants.MQTT_REQUEST_GENERIC_ADLS);
            }
        } else {
            // Use local stored data when it's not possible to connect to the remote server
            loadAdlsScheme();
        }
    }

    // Remove the previous adls scheme in database and save the new in order to have
    // the new version received from the remote DB
    private void updateADLsScheme(final ArrayList<AdlEntity> adlEntities,
                                  final ArrayList<EventEntity> eventEntities,
                                  final ArrayList<EventForAdlEntity> eventsForAdlsEntities) {
        executor.execute(() -> {
            adlDao.deleteAll();
            eventDao.deleteAll();
            eventForAdlDao.deleteAll();

            adlDao.insertAll(adlEntities);
            eventDao.insertAll(eventEntities);
            eventForAdlDao.insertAll(eventsForAdlsEntities);

            loadAdlsScheme();
        });
    }


    // Initialize data structures from the database
    private void initializeMobileDatabase() {
        SensorableDatabase database = MobileDatabaseBuilder.getDatabase(this);

        eventDao = database.eventDao();
        eventForAdlDao = database.eventForAdlDao();
        adlDao = database.adlDao();

        knownLocationDao = database.knownLocationDao();
        adlRegistryDao = database.adlRegistryDao();

        executor = MobileDatabaseBuilder.getExecutor();
    }

    // It does a query to local database and extract from it the data storing it in memory
    // data structures to manage it faster and more efficiently
    private void loadAdlsScheme() {
        executor.execute((() -> {
            events.clear();
            adls.clear();
            eventsForAdls.clear();

            events.addAll(eventDao.getAll());
            adls.addAll(adlDao.getAll());
            eventsForAdls.addAll(eventForAdlDao.getAll());

            // generation of data structure to evaluate adls
            adls.forEach(adlEntity -> {
                HashMap<Integer, ArrayList<Pair<Integer, Boolean>>> adlVersions = new HashMap<>();

                eventsForAdls.forEach(eventForAdlEntity -> {
                    if (eventForAdlEntity.idAdl == adlEntity.id) {

                        int version = eventForAdlEntity.version;
                        if (!adlVersions.containsKey(version)) {
                            adlVersions.put(version, new ArrayList<>());
                        }

                        adlVersions
                                .get(version)
                                .add(new Pair<>(eventForAdlEntity.idEvent, false));
                    }
                });

                databaseAdls.put(adlEntity.id, adlVersions);
            });

            knownLocations.addAll(knownLocationDao.getAll());
        }));
    }

    // This service receives via broadcasting communication the raw data got by the sensors
    private void initializeDataReceiver() {
        dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                ArrayList<SensorTransmissionCoder.SensorData> arrayMessage = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);

                sensorDataBuffer.addAll(arrayMessage);
                if (sensorDataBuffer.size() > SensorableConstants.COLLECTED_SENSOR_DATA_SIZE) {
                    detectAdls(sensorDataBuffer);
                    sensorDataBuffer.clear();
                }

                Log.i("ADL_DETECTION_SERVICE", "received new data " + arrayMessage.size());
            }
        };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.WEAR_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.SENSORS_PROVIDER_SENSORS);
    }

    private void detectAdls(ArrayList<SensorTransmissionCoder.SensorData> data) {
        // first step to detect adl is filtering the received extra larga amount of data
        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorData>> filteredData = filterData(data);

        // second step is evaluate only events and reuse this evaluations
        HashMap<Integer, Boolean> evaluatedEvents = evaluateEvents(filteredData);

        // third step is evaluate the adls once we got the already evaluated events
        evaluateAdls(evaluatedEvents);
    }

    private HashMap<Integer, Boolean> evaluateEvents(HashMap<Long, ArrayList<SensorTransmissionCoder.SensorData>> filteredData) {
        HashMap<Integer, Boolean> evaluatedEvents = new HashMap<>();
        SensorOperations.SensorOperation operation;

        for (EventEntity e : events) {
            for (long timestamp : filteredData.keySet()) {

                // Now, we look into the sensor readings and use sensor that we want to
                // generate conditions and later shoot rules
                for (SensorTransmissionCoder.SensorData s : filteredData.get(timestamp)) {

                    // evaluate the events
                    // TODO remember to check the device type
                    if (s.getSensorType() == e.sensorType) {
                        operation = SensorOperations.switchOperation(e.operator);

                        if (operation != null) {
                            evaluatedEvents.put(e.id, SensorOperations.switchOperate(operation, s.getValue(), e, knownLocations));

                        } else {
                            Log.i("ADL_DETECTION_SERVICE", "null operation, operator bad specified");
                        }
                    }
                }
            }
        }

        return evaluatedEvents;
    }

    // After detecting the events this method detects the adls checking the sequence of events
    private void evaluateAdls(HashMap<Integer, Boolean> evaluatedEvents) {
        // let's take from database adls the events registry owned by each adl
        for (int idCurrentAdl : databaseAdls.keySet()) {
            databaseAdls.get(idCurrentAdl).forEach((version, eventsOfCurrentAdl) -> {
                boolean evaluation = true;
                int size = eventsOfCurrentAdl.size();

                for (int i = 0; i < size; i++) {
                    // check if previous adl in the sorted events array occured, if happened
                    // then we check the next event, the current and update if necessary
                    Pair<Integer, Boolean> event = eventsOfCurrentAdl.get(i);
                    if (!event.second) {
                        /*
                         * Here we want to detect an adl based on the evaluation of the events. An adl
                         * will be true if all of its events are. The events have to be completed in the
                         * exact order they were associated to the adl, so we only check if an event is
                         * true if the previous event was.
                         * If the event is the first or the unique in the array, we supose then that we
                         * have the previous too (because there isn't any previous). After this just look for
                         * the event in the evaluated events array and use its last value.
                         */

                        if ((i == 0 || (i > 0 && eventsOfCurrentAdl.get(i - 1).second) || size == 1) &&
                                evaluatedEvents.containsKey(event.first) && evaluatedEvents.get(event.first)) {
                            eventsOfCurrentAdl.set(i, new Pair<>(event.first, true));

                        } else {
                            // If the value of the current event is not true
                            // then we don't need to check the next values because
                            // they have to be true in the specified order.
                            evaluation = false;
                            break;
                        }
                    }
                }

                if (size > 0 && evaluation) {
                    Log.i("ADL_DETECTION_SERVICE", "recognized a new adl");
                    updateDetectedAdlsRegistries(idCurrentAdl);

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
                } else {
                    Log.i("ADL_DETECTION_SERVICE", "no longer recognized an old adl");
                }
            });

        }
    }

    private void updateDetectedAdlsRegistries(final int idCurrentAdl) {
        // get the last database stored in the adl in the last stage of time
        // if you get one we update the finish timestamp
        // else you add a new row to the database
        executor.execute(() -> {
            long currentTime = new Date().getTime();

            // interval is the current time less 5 minutes, counts made on millis
            long sinceTime = currentTime - SensorableConstants.TIME_SINCE_LAST_ADL_DETECTION;
            AdlRegistryEntity res = adlRegistryDao.getAdlRegistryAfter(idCurrentAdl, sinceTime);

            if (res != null) {
                res.endTime = currentTime;
                adlRegistryDao.update(res);
            } else {
                adlRegistryDao.insert(
                        new AdlRegistryEntity(idCurrentAdl, currentTime, currentTime)
                );
            }
        });
    }

    private HashMap<Long, ArrayList<SensorTransmissionCoder.SensorData>> filterData(ArrayList<SensorTransmissionCoder.SensorData> data) {
        long floorTimestamp;

        HashMap<Long, ArrayList<SensorTransmissionCoder.SensorData>>
                categorization = new HashMap<>();

        // iterate data sensor to categorize each sensor read per timestamp
        for (SensorTransmissionCoder.SensorData newSensorData : data) {
            floorTimestamp = newSensorData.getTimestamp() / SensorableConstants.ADL_FILTER_TIME;


            // if the categorization for these timestamp exists
            if (categorization.containsKey(floorTimestamp)) {
                ArrayList<SensorTransmissionCoder.SensorData>
                        arrayByTimestamp = categorization.get(floorTimestamp);

                boolean found = false;
                ArrayList<SensorTransmissionCoder.SensorData> copyArray = new ArrayList<>(arrayByTimestamp);

                // We want only the last sensor read from each categorization per timestamp
                for (SensorTransmissionCoder.SensorData oldSensorData : arrayByTimestamp) {

                    // we want to know if we have in the same stage (categorization) a repeated sensor data
                    if (oldSensorData.getDeviceType() == newSensorData.getDeviceType()
                            && oldSensorData.getSensorType() == newSensorData.getSensorType()) {

                        // if the current value of this sensor from this device is older than the new
                        // then we replace it removing the old and inserting the new
                        if (oldSensorData.getTimestamp() < newSensorData.getTimestamp()) {
                            copyArray.remove(oldSensorData);
                            copyArray.add(newSensorData);

                            found = true;
                        }
                    }
                }

                if (!found) {
                    copyArray.add(newSensorData);
                }

                categorization.replace(floorTimestamp, copyArray);

            } else {
                ArrayList<SensorTransmissionCoder.SensorData> newArray = new ArrayList<>();
                newArray.add(newSensorData);
                categorization.put(floorTimestamp, newArray);
            }
        }

        return categorization;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}