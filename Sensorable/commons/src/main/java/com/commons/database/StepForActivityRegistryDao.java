package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StepForActivityRegistryDao {
    @Query("SELECT * FROM StepForActivityRegistryEntity")
    List<StepForActivityRegistryEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<StepForActivityRegistryEntity> stepsRegistries);

    @Query("DELETE FROM StepForActivityRegistryEntity")
    void deleteAll();
}