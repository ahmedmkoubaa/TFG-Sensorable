package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ActivityDao {
    @Query("SELECT * FROM ActivityEntity")
    List<ActivityEntity> getAll();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<ActivityEntity> activities);

    @Query("DELETE FROM ActivityEntity")
    void deleteAll();
}