package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdlRegistryDao {
    @Query("SELECT * FROM AdlRegistryEntity")
    List<AdlRegistryEntity> getAll();

    @Query("SELECT * FROM AdlRegistryEntity WHERE `end` > :arg0 ORDER BY `end` DESC LIMIT 1")
    AdlRegistryEntity getAdlRegistryAfter(long arg0);

    @Insert
    void insert(AdlRegistryEntity adlRegistryEntity);

    @Update()
    void update(AdlRegistryEntity adlRegistryEntity);
}
