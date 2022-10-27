package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ActivityStepDao {
    @Query("SELECT * FROM ActivityStepEntity")
    List<ActivityStepEntity> getAll();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<ActivityStepEntity> steps);

    @Query("DELETE FROM ActivityStepEntity")
    void deleteAll();
}