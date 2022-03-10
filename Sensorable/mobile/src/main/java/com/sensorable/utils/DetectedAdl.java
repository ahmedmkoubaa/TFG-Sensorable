package com.sensorable.utils;

import java.util.Date;

public class DetectedAdl {
    private String title;
    private String description;
    private String stats;
    private Date timestamp;

    public DetectedAdl(String title, String description, String stats, Date timestamp) {
        this.title = title;
        this.description = description;
        this.stats = stats;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getStats() {
        return stats;
    }

    public Date getTimestamp() {
        return timestamp;
    }





}
