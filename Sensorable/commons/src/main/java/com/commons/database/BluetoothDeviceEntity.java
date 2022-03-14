package com.commons.database;


import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"address", "first_timestamp"})
public class BluetoothDeviceEntity {
    @NonNull
    public String address;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    @ColumnInfo(name = "bond_state")
    public int bondState;

    @ColumnInfo(name = "bluetooth_device_type")
    public int bluetoothDeviceType;

    @ColumnInfo(name = "trusted")
    public boolean trusted;

    @NonNull
    @ColumnInfo(name = "first_timestamp")
    public long firstTimestamp;

    @NonNull
    @ColumnInfo(name = "last_timestamp")
    public long lastTimestamp;

    public BluetoothDeviceEntity() {

    }

    public BluetoothDeviceEntity(BluetoothDevice device, long firstTimestamp) {
        this.address = device.getAddress();
        this.deviceName = device.getName();
        this.bondState = device.getBondState();
        this.bluetoothDeviceType = device.getBluetoothClass().getDeviceClass();

        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = firstTimestamp;

        this.trusted = false;


    }
}




