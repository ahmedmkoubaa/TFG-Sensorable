package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SensorMessageDao {
    @Query("SELECT * FROM SensorMessageEntity")
    List<SensorMessageEntity> getAll();

    @Query("SELECT * FROM SensorMessageEntity " +
            "WHERE device_type = :arg0 AND sensor_type = :arg1 AND timestamp = :arg2")
    SensorMessageEntity findByKey(int arg0, int arg1, long arg2);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SensorMessageEntity device);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<SensorMessageEntity> device);
}