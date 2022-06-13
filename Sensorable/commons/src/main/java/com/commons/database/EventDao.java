package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM EventEntity")
    List<EventEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<EventEntity> events);

    @Query("DELETE FROM EventEntity")
    void deleteAll();
}
