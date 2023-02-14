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
public interface ActivityDao {
    @Query("SELECT * FROM ActivityEntity")
    List<ActivityEntity> getAll();

    @Query("SELECT COUNT(*) FROM ActivityEntity")
    int size();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<ActivityEntity> activities);

    @Delete
    void delete(List<ActivityEntity> activities);

    @Update
    void update(List<ActivityEntity> activities);
}