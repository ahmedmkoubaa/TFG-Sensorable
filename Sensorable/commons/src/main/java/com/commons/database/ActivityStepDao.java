package com.commons.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ActivityStepDao {
    @Query("SELECT * FROM ActivityStepEntity")
    List<ActivityStepEntity> getAll();

    @Query("SELECT COUNT(*) FROM ActivityStepEntity")
    int size();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<ActivityStepEntity> steps);

    @Query("SELECT * FROM ActivityStepEntity WHERE id = :arg0")
    ActivityStepEntity getStepById(int arg0);

    @Query("SELECT * FROM ActivityStepEntity " +
            "WHERE id IN " +
            "(SELECT id_step FROM StepsForActivitiesEntity WHERE id_activity = :arg0 )")
    List<ActivityStepEntity> getStepsOfActivity(long arg0);

    @Update
    void update(ArrayList<ActivityStepEntity> stepsEntities);

    @Delete
    void delete(List<ActivityStepEntity> collect);
}