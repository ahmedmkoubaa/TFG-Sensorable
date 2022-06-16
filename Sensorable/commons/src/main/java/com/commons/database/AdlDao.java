package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AdlDao {
    @Query("SELECT * FROM AdlEntity")
    List<AdlEntity> getAll();

    @Query("SELECT * FROM AdlEntity WHERE id = :arg0")
    AdlEntity getAdlById(int arg0);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ArrayList<AdlEntity> adls);

    @Query("DELETE FROM AdlEntity")
    void deleteAll();
}