package com.commons.database;


import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"address"})
public class BluetoothDeviceEntity {
    @NonNull
    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    @ColumnInfo(name = "bond_state")
    public int bondState;

    @ColumnInfo(name = "bluetooth_device_type")
    public int bluetoothDeviceType;

    @ColumnInfo(name = "trusted")
    public boolean trusted;


    public BluetoothDeviceEntity() {

    }
    public BluetoothDeviceEntity(BluetoothDevice device) {
        this.address = device.getAddress();
        this.deviceName = device.getName();
        this.bondState = device.getBondState();
        this.bluetoothDeviceType = device.getBluetoothClass().getDeviceClass();
        this.trusted = false;
    }

    public BluetoothDeviceEntity(String address, String name, int bondState, int bluetoothDeviceType, boolean trusted) {
        this.address = address;
        this.deviceName = name;
        this.bondState = bondState;
        this.bluetoothDeviceType = bluetoothDeviceType;
        this.trusted = trusted;
    }
}




