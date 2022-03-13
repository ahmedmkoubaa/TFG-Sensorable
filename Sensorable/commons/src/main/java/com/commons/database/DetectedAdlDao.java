package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectedAdlDao {
    @Query("SELECT * FROM DetectedAdlEntity ORDER BY timestamp DESC")
    List<DetectedAdlEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DetectedAdlEntity detectedAdl);
}
