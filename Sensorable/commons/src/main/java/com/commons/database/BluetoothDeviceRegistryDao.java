package com.commons.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface BluetoothDeviceRegistryDao {
    @Insert
    void insert(BluetoothDeviceRegistryEntity device);

    @Query("SELECT * FROM BluetoothDeviceRegistryEntity " +
            "WHERE (start <= :arg0 AND ( (:arg1 <=  `end`) OR (start = `end`)))" +
            "ORDER BY `end` DESC LIMIT 1")
    BluetoothDeviceRegistryEntity getDevicesInRange(long arg0, long arg1);

    @Query("SELECT * FROM BluetoothDeviceRegistryEntity")
    List<BluetoothDeviceRegistryEntity> getAll();

    @Update()
    void updateDevice(BluetoothDeviceRegistryEntity device);
}
