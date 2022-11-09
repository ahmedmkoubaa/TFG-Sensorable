package com.sensorable.utils;

import android.content.Context;

import androidx.room.Room;

import com.commons.SensorableConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobileDatabaseBuilder {
    private static MobileDatabase database;
    public static MobileDatabase getDatabase(Context context) {
        if (database == null) {
            database = Room.databaseBuilder(
                    context,
                    MobileDatabase.class,
                    SensorableConstants.MOBILE_DATABASE_NAME
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
