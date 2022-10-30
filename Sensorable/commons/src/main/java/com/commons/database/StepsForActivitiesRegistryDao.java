package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StepsForActivitiesRegistryDao {
    @Query("SELECT * FROM StepsForActivitiesRegistryEntity")
    List<StepsForActivitiesRegistryEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<StepsForActivitiesRegistryEntity> stepsRegistries);

    @Query("DELETE FROM StepsForActivitiesRegistryEntity")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StepsForActivitiesRegistryEntity stepsRegistries);
}