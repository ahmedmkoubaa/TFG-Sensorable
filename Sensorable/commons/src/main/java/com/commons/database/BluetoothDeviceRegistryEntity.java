package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        primaryKeys = {"address", "start"},
        foreignKeys = {
                @ForeignKey(entity = BluetoothDeviceEntity.class,
                        parentColumns = "address",
                        childColumns = "address",
                        onDelete = ForeignKey.CASCADE)
        })

public class BluetoothDeviceRegistryEntity {
    @NonNull
    @ColumnInfo(name = "address")
    public String address;

    @NonNull
    @ColumnInfo(name = "start")
    public long start;

    @NonNull
    @ColumnInfo(name = "end")
    public long end;

    public BluetoothDeviceRegistryEntity(String address, long start) {
        this.address = address;
        this.start = start;
        this.end = start;
    }
}




