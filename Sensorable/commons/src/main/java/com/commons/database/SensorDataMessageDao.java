package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SensorDataMessageDao {
    @Query("SELECT * FROM SensorDataMessage")
    List<SensorDataMessage> getAll();

    @Query("SELECT * FROM SensorDataMessage " +
            "WHERE device_type = :arg0 AND sensor_type = :arg1 AND timestamp = :arg2")
    SensorDataMessage findByKey(int arg0, int arg1, long arg2);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SensorDataMessage device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<SensorDataMessage> device);
}