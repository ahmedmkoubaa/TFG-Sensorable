package com.sensorable.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.database.SensorMessageDao;
import com.commons.database.SensorMessageEntity;
import com.commons.utils.DeviceType;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableDatabase;
import com.commons.utils.SensorableIntentFilters;
import com.opencsv.CSVWriter;
import com.sensorable.utils.WearDatabaseBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class DataSenderService extends Service {
    private SensorableDatabase database;
    private SensorMessageDao sensorMessageDao;
    private ExecutorService executor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        PutDataMapRequest dataMap = PutDataMapRequest.create("/data");
//        dataMap.getDataMap().putString("message", "Hello from Wear OS!");
//        Task<DataItem> putDataTask = Wearable.getDataClient(getApplicationContext()).putDataItem(dataMap.asPutDataRequest());


        initializeDatabase();
        initializeReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeDatabase() {
        database = WearDatabaseBuilder.getDatabase(this);
        sensorMessageDao = database.sensorMessageDao();
        executor = WearDatabaseBuilder.getExecutor();
    }

    public void exportToCsv(final List<SensorMessageEntity> sensorMessages) {
        // filter by device type and by sensor type dynamically using the already defined data types
        getDevicesNames().stream().forEach(device -> {
           SensorableConstants.LISTENED_SENSORS.forEach(sensor -> {
               ArrayList<SensorMessageEntity> filteredArray = sensorMessages.stream()
                       .filter(sensorMessage -> sensorMessage.deviceType == device.first)
                       .filter(sensorMessage -> sensorMessage.sensorType == sensor.first)
                       .collect(Collectors.toCollection(ArrayList::new));

               if (!filteredArray.isEmpty()) {
                   String composedPath = device.second + SensorableConstants.FILE_PATH_SEPARATOR + sensor.second;
                   exportToCsv(filteredArray, composedPath);
               }
           });
        });
    }

    private ArrayList<Pair<Integer, String>> getDevicesNames() {
        ArrayList<Pair<Integer, String>> arrayFields = new ArrayList<>();

        // Get the names of the static fields in the MyClass class
        Field[] fields = DeviceType.class.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    arrayFields.add(new Pair(field.get(null), field.getName()));
                } catch (IllegalAccessException e) {
                    Log.e("EXPORT CSV", "HAS FAILED BECAUSE OF A NULL FIELD VALUE");
                }
            }
        }

        return arrayFields;
    }

    public void exportToCsv(List<SensorMessageEntity> sensorMessages, final String name) {
        try {
            File exportDir = new File(Environment.getExternalStorageDirectory(), SensorableConstants.ROOT_DIRECTOTY_NAME);
            exportDir.mkdirs();
            exportDir.delete();

            File[] array = exportDir.listFiles();


            String fileName = name + SensorableConstants.FILE_EXTENSION_SEPARATOR + SensorableConstants.CSV_EXTENSION;
            File exportFile = new File(exportDir.getAbsolutePath() + SensorableConstants.FILE_PATH_SEPARATOR + fileName);

            File parentDir = exportFile.getParentFile();
            parentDir.mkdirs();

            exportFile.mkdirs();
            exportFile.createNewFile();

            if (exportFile.isDirectory()) {
                Log.e("WE HAVE A FREAKING" , "PROBLEM");
            }

            FileWriter newFile = new FileWriter(exportFile);
            CSVWriter writer = new CSVWriter(newFile);

            for (SensorMessageEntity sensorMessage : sensorMessages) {
                String[] row = {
                        String.valueOf(sensorMessage.deviceType),
                        String.valueOf(sensorMessage.sensorType),
                        String.valueOf(sensorMessage.valuesX),
                        String.valueOf(sensorMessage.valuesY),
                        String.valueOf(sensorMessage.valuesZ),
                        String.valueOf(sensorMessage.timestamp),
                        sensorMessage.userId
                };
                writer.writeNext(row);
            }
            writer.close();

        } catch (IOException e) {
            Log.e("WEAR OS DATA SENDER", "FAILURE SAVING DATA" + e.getMessage());
        }
    }



    private void initializeReceiver() {
        // To receive data and store it using local database
        LocalBroadcastManager.getInstance(this).
                registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                                ArrayList<SensorTransmissionCoder.SensorData> sensorsArray = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);
                                executor.execute(() -> {
                                    ArrayList<SensorMessageEntity> newSensors = SensorTransmissionCoder.SensorData.toSensorDataMessages(sensorsArray);
                                    sensorMessageDao.insertAll(newSensors);
                                    exportToCsv(newSensors);
                                    int size = sensorMessageDao.getAll().size();

                                    Log.i("asdad", "size is " + size);
                                });

                                Log.i("DATA SENDER SERVICE", "received sensors size is" + sensorsArray.size());

                            }


                        }, SensorableIntentFilters.SERVICE_PROVIDER_SENSORS);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}