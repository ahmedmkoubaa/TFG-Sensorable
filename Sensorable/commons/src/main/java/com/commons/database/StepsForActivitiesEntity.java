package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(foreignKeys = {
        @ForeignKey(entity = ActivityEntity.class,
                parentColumns = "id",
                childColumns = "id_activity",
                onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(entity = ActivityStepEntity.class,
                parentColumns = "id",
                childColumns = "id_step",
                onDelete = ForeignKey.CASCADE
        )},
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepsForActivitiesEntity StepsForActivitiesEntity = (StepsForActivitiesEntity) o;
        return id == StepsForActivitiesEntity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idActivity, idStep);
    }


    public StepsForActivitiesEntity(@NonNull int id, @NonNull int idActivity, @NonNull int idStep) {
        this.id = id;
        this.idActivity = idActivity;
        this.idStep = idStep;
    }
}