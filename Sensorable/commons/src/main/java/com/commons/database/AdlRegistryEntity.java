package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        primaryKeys = {"id_adl", "start"}
//        TODO: define a better scheme to handle the database foreign keys
//        the referred foreign key is going to change so many times so we need
//        it to stay always the same or to be only replaced and not deleted at all
//        foreignKeys = {
//                @ForeignKey(entity = AdlEntity.class,
//                        parentColumns = "id",
//                        childColumns = "id_adl",
//                        onDelete = ForeignKey.RESTRICT)
//        }
        )
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

    public AdlRegistryEntity(@NonNull int idAdl, @NonNull long startTime, @NonNull long endTime) {
        this.idAdl = idAdl;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}