package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(foreignKeys = {
        @ForeignKey(entity = AdlEntity.class,
                parentColumns = "id",
                childColumns = "id_adl",
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = EventEntity.class,
                parentColumns = "id",
                childColumns = "id_event",
                onDelete = ForeignKey.CASCADE)
})
public class EventForAdlEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "id_adl")
    public int idAdl;

    @NonNull
    @ColumnInfo(name = "id_event")
    public int idEvent;

    public EventForAdlEntity(@NonNull int id, @NonNull int idAdl, @NonNull int idEvent) {
        this.id = id;
        this.idAdl = idAdl;
        this.idEvent = idEvent;
    }
}





