package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdlRegistryDao {
    @Query("SELECT * FROM AdlRegistryEntity ORDER BY `end` DESC")
    List<AdlRegistryEntity> getAll();

    @Query("SELECT * FROM AdlRegistryEntity WHERE id_adl = :arg0 AND `end` > :arg1 ORDER BY `end` DESC LIMIT 1")
    AdlRegistryEntity getAdlRegistryAfter(int arg0, long arg1);

    @Insert
    void insert(AdlRegistryEntity adlRegistryEntity);

    @Update()
    void update(AdlRegistryEntity adlRegistryEntity);
}
