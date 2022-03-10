package com.commons.database;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class BluetoothDevice {
    @NonNull
    @PrimaryKey
    public String address;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    @ColumnInfo(name = "bond_state")
    public int bondState;

    @ColumnInfo(name = "bluetooth_device_type")
    public int bluetoothDeviceType;

    @ColumnInfo(name = "trusted")
    public boolean trusted;
}




