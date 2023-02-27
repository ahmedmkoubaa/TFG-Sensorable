package com.commons.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.commons.database.SensorMessageEntity;
import com.commons.utils.DeviceType;
import com.commons.utils.LoginHelper;
import com.commons.utils.SensorTransmissionCoder;
import com.commons.utils.SensorableConstants;
import com.commons.utils.SensorableIntentFilters;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CsvSaverService extends Service {

    private final ArrayList<SensorMessageEntity> sensorBufferToExportCSV = new ArrayList();
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initializeReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initializeReceiver() {
        // To receive data and store it using local database
        final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getBundleExtra(SensorableConstants.EXTRA_MESSAGE);
                ArrayList<SensorTransmissionCoder.SensorData> sensorsArray = b.getParcelableArrayList(SensorableConstants.BROADCAST_MESSAGE);

                sensorBufferToExportCSV.addAll(SensorTransmissionCoder.SensorData.toSensorDataMessages(sensorsArray));

                if (sensorBufferToExportCSV.size() > SensorableConstants.MAX_COLLECTED_DATA_EXPORT_CSV) {
                    exportToCsv(sensorBufferToExportCSV);
                    sensorBufferToExportCSV.clear();

                }
            }
        };

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.SERVICE_PROVIDER_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.EMPATICA_SENSORS);

        LocalBroadcastManager.getInstance(this).
                registerReceiver(dataReceiver, SensorableIntentFilters.WEAR_SENSORS);
    }

    // Created the data structure and files organization in order to save
    // the CSV. It creates a default folder for the system and then creates
    // folders with the user_id and into that folders a folder for each device type
    // and into this folders a regular csv file for each sensor type.
    public void exportToCsv(final List<SensorMessageEntity> sensorMessages) {
        // filter by device type and by sensor type dynamically using the already defined data types
        getDevicesNames().stream().forEach(device -> {
            SensorableConstants.LISTENED_SENSORS.forEach(sensor -> {
                ArrayList<SensorMessageEntity> filteredArray = sensorMessages.stream()
                        .filter(sensorMessage -> sensorMessage.deviceType == device.first)
                        .filter(sensorMessage -> sensorMessage.sensorType == sensor.first)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (!filteredArray.isEmpty()) {
                    // pass the base path using the usercode and the deviceType
                    // then pass the sensor name stored in sensor.second to have a file name
                    String userCode =  Objects.toString(LoginHelper.getUserCode(this), "NULL");
                    String basePath = userCode + SensorableConstants.FILE_PATH_SEPARATOR +  device.second;
                    exportToCsv(filteredArray,  basePath, sensor.second);
                }
            });
        });
    }

    // It receives the data and the path of the file where it's necessary to store the data
    // if the file path doesn't exists it creates it to save the data as a CSV
    public void exportToCsv(List<SensorMessageEntity> sensorMessages, final String path, final String fileName) {
        File exportDir = new File(Environment.getExternalStorageDirectory(), SensorableConstants.ROOT_DIRECTOTY_NAME + SensorableConstants.FILE_PATH_SEPARATOR + path) ;
        exportDir.mkdirs();

        File exportFile = new File(exportDir.getAbsolutePath(), fileName + SensorableConstants.FILE_EXTENSION_SEPARATOR + SensorableConstants.CSV_EXTENSION);

        try {
            exportFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(exportFile));

            for (SensorMessageEntity sensorMessage : sensorMessages) {
                String[] row = {
                        String.valueOf(sensorMessage.valuesX),
                        String.valueOf(sensorMessage.valuesY),
                        String.valueOf(sensorMessage.valuesZ),
                        String.valueOf(sensorMessage.timestamp),
                };
                writer.writeNext(row);
            }
            writer.close();

        } catch (IOException e) {
            Log.e("CSV SAVER SERVICE", "FAILURE SAVING DATA" + e.getMessage());
            deleteDirectory(exportDir);
        }
    }

    // It gets dynamically the device types and returns a list of pairs
    // where the first element is the device code and the second the device name
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


    // Deletes all directories recursively beginning from passed directory
    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}