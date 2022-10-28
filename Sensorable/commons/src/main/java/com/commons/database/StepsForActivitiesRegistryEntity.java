package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = StepsForActivitiesEntity.class,
                parentColumns = "id",
                childColumns = "id_step_for_activity",
                onDelete = ForeignKey.CASCADE)
})
public class StepsForActivitiesRegistryEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "id_step_for_activity")
    public int idStepForActivity;

    @NonNull
    @ColumnInfo(name = "timestamp")
    public long timestamp;


    public StepsForActivitiesRegistryEntity(@NonNull int id, @NonNull int idStepForActivity, @NonNull long timestamp) {
        this.id = id;
        this.idStepForActivity = idStepForActivity;
        this.timestamp = timestamp;
    }
}