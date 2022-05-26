package com.sensorable.utils;

import android.content.Context;

import androidx.room.Room;

import com.commons.SensorableConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MobileDatabaseBuilder {
    public static MobileDatabase getDatabase(Context context) {
        return Room.databaseBuilder(
                context,
                MobileDatabase.class,
                SensorableConstants.MOBILE_DATABASE_NAME
        ).fallbackToDestructiveMigration() // TODO remove this option
                .build();
    }

    public static ExecutorService getExecutor() {
        return Executors.newFixedThreadPool(SensorableConstants.MOBILE_DATABASE_NUMBER_THREADS);
    }
}
