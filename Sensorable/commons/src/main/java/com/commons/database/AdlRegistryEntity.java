package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        primaryKeys = {"id_adl", "start"},
        foreignKeys = {
                @ForeignKey(entity = AdlEntity.class,
                        parentColumns = "id",
                        childColumns = "id_adl",
                        onDelete = ForeignKey.CASCADE)
        })
public class AdlRegistryEntity {
    @NonNull
    @ColumnInfo(name = "id_adl")
    public int idAdl;

    @NonNull
    @ColumnInfo(name = "start")
    public long startTime;

    @NonNull
    @ColumnInfo(name = "end")
    public long endTime;

    public AdlRegistryEntity(int idAdl, long startTime, long endTime) {
        this.idAdl = idAdl;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}





