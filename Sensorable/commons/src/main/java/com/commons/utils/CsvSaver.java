package com.commons.utils;

import android.os.Environment;
import android.util.Log;

import com.commons.database.SensorMessageEntity;
import com.commons.database.StepsForActivitiesRegistryEntity;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
}
