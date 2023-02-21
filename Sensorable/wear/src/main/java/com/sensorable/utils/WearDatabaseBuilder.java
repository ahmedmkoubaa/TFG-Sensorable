package com.sensorable.utils;

import android.content.Context;

import androidx.room.Room;

import com.commons.utils.SensorableConstants;
import com.commons.database.SensorableDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WearDatabaseBuilder {
    private static SensorableDatabase database;
    public static SensorableDatabase getDatabase(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(
                    context,
                    SensorableDatabase.class,
                    SensorableConstants.WEAR_DATABASE_NAME
            )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public static ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(SensorableConstants.MOBILE_DATABASE_NUMBER_THREADS);
    }
}