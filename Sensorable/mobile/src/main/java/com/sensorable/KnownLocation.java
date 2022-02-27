package com.sensorable;

import org.osmdroid.util.GeoPoint;

public class KnownLocation {
    private String title;
    private String address;
    private String tag;
    private GeoPoint point;

    public KnownLocation(String title, String address, GeoPoint point, String tag) {
        this.title = title;
        this.address = address;
        this.point = point;
        this.tag = tag;
    }


    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getTag() {
        return tag;
    }

    public GeoPoint getPoint() {
        return point;
    }

}
