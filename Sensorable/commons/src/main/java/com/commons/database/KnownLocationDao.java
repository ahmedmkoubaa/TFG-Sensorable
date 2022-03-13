package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface KnownLocationDao {
    @Query("SELECT * FROM KnownLocationEntity")
    List<KnownLocationEntity> getAll();

    @Insert
    void insert(KnownLocationEntity entity);

    @Update
    void updateDevice(KnownLocationEntity entity);
}