package com.example.commons.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BluetoothDeviceDao {
    @Query("SELECT * FROM BluetoothDevice")
    List<BluetoothDevice> getAll();

    @Query("SELECT * FROM BluetoothDevice WHERE address IN (:arg0)")
    List<BluetoothDevice> loadAllByIds(String[] arg0);

    @Query("SELECT * FROM BluetoothDevice WHERE trusted = 1")
    List<BluetoothDevice> findTrusted();

    @Query("SELECT * FROM BluetoothDevice WHERE address LIKE :arg0 ")
    BluetoothDevice findByAddress(String arg0);

    @Query("SELECT * FROM BluetoothDevice WHERE device_name LIKE :arg0 LIMIT 1")
    BluetoothDevice findByName(String arg0);

    @Insert
    void insertAll(BluetoothDevice... devices);

    @Insert
    void insert(BluetoothDevice device);

    @Delete
    void delete(BluetoothDevice device);

    @Update
    void updateDevice(BluetoothDevice device);
}