package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DetectedAdlDao {
    @Query("SELECT * FROM DetectedAdlEntity ORDER BY first_timestamp DESC")
    List<DetectedAdlEntity> getAll();

    @Query("SELECT * FROM DetectedAdlEntity " +
            "WHERE title == :arg0 AND last_timestamp >= :arg1 " +
            "ORDER BY last_timestamp DESC LIMIT 1 ")
    DetectedAdlEntity getLastAdl(String arg0, long arg1);

    @Update
    void update(DetectedAdlEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DetectedAdlEntity detectedAdl);
}
