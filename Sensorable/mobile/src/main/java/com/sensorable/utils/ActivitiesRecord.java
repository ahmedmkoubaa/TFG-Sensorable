package com.sensorable.utils;


public class ActivitiesRecord {
    private int id;
    private final String title;
    private final String description;

    public ActivitiesRecord(final int id, final String title, final String description) {
        this.id = id;
        this.title = title;
        this.description = description;

    }
    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }


}
