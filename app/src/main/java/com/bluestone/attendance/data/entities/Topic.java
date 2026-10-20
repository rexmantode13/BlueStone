package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "topics",
    foreignKeys = @ForeignKey(
        entity = SyllabusUnit.class,
        parentColumns = "id",
        childColumns = "unit_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index(value = {"unit_id"})}
)
public class Topic {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "unit_id")
    private long unitId;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private boolean isCompleted;

    public Topic(long unitId, @NonNull String title, boolean isCompleted) {
        this.unitId = unitId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUnitId() { return unitId; }
    public void setUnitId(long unitId) { this.unitId = unitId; }

    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
