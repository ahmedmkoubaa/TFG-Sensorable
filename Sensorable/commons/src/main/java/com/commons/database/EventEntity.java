package com.commons.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.commons.OperatorType;

@Entity
public class EventEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @NonNull
    @ColumnInfo(name = "adl")
    public int deviceType;

    @NonNull
    @ColumnInfo(name = "sensor_type")
    public int sensorType;

    @NonNull
    @ColumnInfo(name = "pos")
    public int pos;

    @NonNull
    @ColumnInfo(name = "operator")
    public OperatorType operator;

    @NonNull
    @ColumnInfo(name = "operand")
    public float operand;

    @ColumnInfo(name = "tag")
    public String tag;

    public EventEntity(@NonNull int id, @NonNull int deviceType, @NonNull int sensorType, @NonNull int pos, @NonNull OperatorType operator , @NonNull float operand, String tag) {
        this.id = id;
        this.deviceType = deviceType;
        this.sensorType = sensorType;
        this.pos = pos;
        this.operator = operator;
        this.operand = operand;
        this.tag = tag;
    }
}