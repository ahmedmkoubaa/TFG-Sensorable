package com.commons.database;

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

    @Query("SELECT * FROM BluetoothDevice WHERE address LIKE :arg0 ")
    BluetoothDevice findByAddress(String arg0);

    @Insert
    void insert(BluetoothDevice device);

    @Update
    void updateDevice(BluetoothDevice device);
}