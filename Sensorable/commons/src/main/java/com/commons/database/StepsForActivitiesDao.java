package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StepsForActivitiesDao {
    @Query("SELECT * FROM StepsForActivitiesEntity")
    List<StepsForActivitiesEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<StepsForActivitiesEntity> stepsForActivities);

    @Query("DELETE FROM StepsForActivitiesEntity")
    void deleteAll();
}