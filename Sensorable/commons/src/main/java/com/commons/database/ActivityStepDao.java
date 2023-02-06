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

    @Query("SELECT * FROM ActivityStepEntity WHERE id = :arg0")
    ActivityStepEntity getStepById(int arg0);

    @Query("SELECT * FROM ActivityStepEntity " +
            "WHERE id IN " +
            "(SELECT id_step FROM StepsForActivitiesEntity WHERE id_activity = :arg0 )")
    List<ActivityStepEntity> getStepsOfActivity(long arg0);

}