package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "syllabus_units",
    foreignKeys = @ForeignKey(
        entity = Subject.class,
        parentColumns = "id",
        childColumns = "subject_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"subject_id", "unit_number"}, unique = true),
        @Index(value = {"subject_id"})
    }
)
public class SyllabusUnit {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "subject_id")
    private long subjectId;

    @ColumnInfo(name = "unit_number")
    private int unitNumber;

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "topics")
    private String topics;

    @ColumnInfo(name = "objectives")
    private String objectives;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private boolean isCompleted;

    public SyllabusUnit(long subjectId, int unitNumber, @NonNull String title,
                        String topics, String objectives, String notes, boolean isCompleted) {
        this.subjectId = subjectId;
        this.unitNumber = unitNumber;
        this.title = title;
        this.topics = topics;
        this.objectives = objectives;
        this.notes = notes;
        this.isCompleted = isCompleted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSubjectId() { return subjectId; }
    public void setSubjectId(long subjectId) { this.subjectId = subjectId; }

    public int getUnitNumber() { return unitNumber; }
    public void setUnitNumber(int unitNumber) { this.unitNumber = unitNumber; }

    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getTopics() { return topics; }
    public void setTopics(String topics) { this.topics = topics; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
