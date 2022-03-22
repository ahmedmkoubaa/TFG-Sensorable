package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.osmdroid.util.GeoPoint;


@Entity
public class KnownLocationEntity {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "tag")
    public String tag;

    @ColumnInfo(name = "altitude")
    public double altitude;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    public KnownLocationEntity() {
    }

    public KnownLocationEntity(String title, String address, String tag, GeoPoint point) {
        this.title = title;
        this.address = address;
        this.tag = tag;
        this.altitude = point.getAltitude();
        this.latitude = point.getLatitude();
        this.longitude = point.getLongitude();
    }
}



