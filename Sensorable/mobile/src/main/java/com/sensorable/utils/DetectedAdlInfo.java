package com.sensorable.utils;


public class DetectedAdlInfo {
    private final int idAdl;
    private final long startTime;
    private final long endTime;

    private final String title;
    private final String description;

    private final boolean accompanied;

    public DetectedAdlInfo(int idAdl, String title, String description, long startTime, long endTime,  boolean accompanied) {
        this.idAdl = idAdl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
        this.accompanied = accompanied;
    }

    public int getIdAdl() {
        return idAdl;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean getAccompanied() {
        return accompanied;
    }


}
