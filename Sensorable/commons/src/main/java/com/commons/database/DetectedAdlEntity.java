package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"title", "first_timestamp"})
public class DetectedAdlEntity {
    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "stats")
    public String stats;

    @NonNull
    @ColumnInfo(name = "first_timestamp")
    public long firstTimestamp;

    @NonNull
    @ColumnInfo(name = "last_timestamp")
    public long lastTimestamp;

    @ColumnInfo(name = "accompanied")
    public boolean accompanied;

    public DetectedAdlEntity(@NonNull String title, String description, String stats, @NonNull long firstTimestamp, long lastTimestamp, boolean accompanied) {
        this.title = title;
        this.description = description;
        this.stats = stats;
        this.firstTimestamp = firstTimestamp;
        this.lastTimestamp = lastTimestamp;
        this.accompanied = accompanied;
    }

}
