package com.commons.utils;

import android.os.Environment;
import android.util.Log;

import androidx.core.util.Pair;

import com.commons.database.SensorMessageEntity;
import com.commons.database.StepsForActivitiesRegistryEntity;
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

public class CsvSaver {
    // It receives the data and the path of the file where it's necessary to store the data
    // if the file path doesn't exists it creates it to save the data as a CSV
    public static void exportSensorsToCsv(List<SensorMessageEntity> sensorMessages, final String path, final String fileName) {
        File exportDir = new File(Environment.getExternalStorageDirectory(), SensorableConstants.ROOT_DIRECTOTY_NAME + SensorableConstants.FILE_PATH_SEPARATOR + path);
        exportDir.mkdirs();

        File exportFile = new File(exportDir.getAbsolutePath(), fileName + SensorableConstants.FILE_EXTENSION_SEPARATOR + SensorableConstants.CSV_EXTENSION);

        try {
            exportFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(exportFile, true));

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

    public static void exportActivityToCSV(String userCode, StepsForActivitiesRegistryEntity activity) {
        // path to the file with the needed directories
        final String path =
                userCode + SensorableConstants.FILE_PATH_SEPARATOR +
                        SensorableConstants.ACTIVITIES_REGISTRY_PATH + SensorableConstants.FILE_PATH_SEPARATOR;

        // name of the file without extension
        final String fileName = SensorableConstants.ACTIVITIES_FILE_NAME;

        try {
            CSVWriter writer = new CSVWriter(new FileWriter(createDirectoriesOrganization(path, fileName), true));
            String[] row = {
                    String.valueOf(activity.idActivity),
                    String.valueOf(activity.idStep),
                    String.valueOf(activity.timestamp),
            };
            writer.writeNext(row);

            writer.close();

        } catch (IOException e) {
            Log.e("CSV SAVER", "FAILURE SAVING DATA" + e.getMessage());
        }


    }

    private static File createDirectoriesOrganization(final String directoryPath, final String fileName) throws IOException {
        File exportDir = new File(Environment.getExternalStorageDirectory(), SensorableConstants.ROOT_DIRECTOTY_NAME + SensorableConstants.FILE_PATH_SEPARATOR + directoryPath);
        exportDir.mkdirs();

        File exportFile = new File(
                exportDir.getAbsolutePath(),
                fileName + SensorableConstants.FILE_EXTENSION_SEPARATOR + SensorableConstants.CSV_EXTENSION
        );
        exportFile.createNewFile();

        return exportFile;
    }

    // Created the data structure and files organization in order to save
    // the CSV. It creates a default folder for the system and then creates
    // folders with the user_id and into that folders a folder for each device type
    // and into this folders a regular csv file for each sensor type.
    public static void exportToCsv(final List<SensorMessageEntity> sensorMessages, final String userCode) {
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

                    String basePath = userCode + SensorableConstants.FILE_PATH_SEPARATOR + device.second;
                    CsvSaver.exportSensorsToCsv(filteredArray, basePath, sensor.second);
                }
            });
        });
    }

    // It gets dynamically the device types and returns a list of pairs
    // where the first element is the device code and the second the device name
    private static ArrayList<Pair<Integer, String>> getDevicesNames() {
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
}
