package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BluetoothDeviceDao {
    @Query("SELECT * FROM BluetoothDeviceEntity")
    List<BluetoothDeviceEntity> getAll();

    @Query("SELECT * FROM BluetoothDeviceEntity WHERE address LIKE :arg0 ")
    BluetoothDeviceEntity findByAddress(String arg0);

    @Insert
    void insert(BluetoothDeviceEntity device);

    @Update
    void updateDevice(BluetoothDeviceEntity device);
}