package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = ActivityEntity.class,
                parentColumns = "id",
                childColumns = "id_activity",
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = ActivityStepEntity.class,
                parentColumns = "id",
                childColumns = "id_step",
                onDelete = ForeignKey.CASCADE)
})

public class StepForActivityEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "id_activity")
    public int idActivity;

    @NonNull
    @ColumnInfo(name = "id_step")
    public int idStep;


    public StepForActivityEntity(@NonNull int id, @NonNull int idActivity, @NonNull int idStep) {
        this.id = id;
        this.idActivity = idActivity;
        this.idStep = idStep;
    }
}