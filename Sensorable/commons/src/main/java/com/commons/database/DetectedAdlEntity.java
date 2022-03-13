package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"title", "timestamp"})
public class DetectedAdlEntity {
    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "stats")
    public String stats;

    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    public DetectedAdlEntity(@NonNull String title, String description, String stats, @NonNull long timestamp) {
        this.title = title;
        this.description = description;
        this.stats = stats;
        this.timestamp = timestamp;
    }

}
