package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectedAdlDao {
    @Query("SELECT * FROM DetectedAdlEntity")
    List<DetectedAdlEntity> getAll();

    @Insert
    void insert(DetectedAdlEntity detectedAdl);
}
