package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StepForActivityDao {
    @Query("SELECT * FROM StepForActivityEntity")
    List<StepForActivityEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<StepForActivityEntity> stepsForActivities);

    @Query("DELETE FROM StepForActivityEntity")
    void deleteAll();
}