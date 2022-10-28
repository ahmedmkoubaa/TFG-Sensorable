package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = ActivityEntity.class,
                parentColumns = "id",
                childColumns = "id_activity",
                onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = ActivityStepEntity.class,
                parentColumns = "id",
                childColumns = "id_step",
                onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index(value = {"id_activity", "id_step"})}
)
public class StepsForActivitiesEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "id_activity")
    public int idActivity;

    @NonNull
    @ColumnInfo(name = "id_step")
    public int idStep;


    public StepsForActivitiesEntity(@NonNull int id, @NonNull int idActivity, @NonNull int idStep) {
        this.id = id;
        this.idActivity = idActivity;
        this.idStep = idStep;
    }
}