package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class ActivityEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @NonNull
    @ColumnInfo(name = "description")
    public String description;

    public ActivityEntity(@NonNull int id, @NonNull String title, @NonNull String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}