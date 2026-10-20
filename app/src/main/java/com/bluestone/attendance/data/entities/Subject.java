package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "subjects",
    indices = {@Index(value = {"code"}, unique = false)}
)
public class Subject {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "semester")
    private int semester;

    @ColumnInfo(name = "credits")
    private int credits;

    @ColumnInfo(name = "faculty")
    private String faculty;

    @ColumnInfo(name = "target_percentage")
    private double targetPercentage;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    private boolean isArchived;

    public Subject(@NonNull String name, @NonNull String code, int semester,
                   int credits, String faculty, double targetPercentage,
                   String notes, boolean isArchived) {
        this.name = name;
        this.code = code;
        this.semester = semester;
        this.credits = credits;
        this.faculty = faculty;
        this.targetPercentage = targetPercentage;
        this.notes = notes;
        this.isArchived = isArchived;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getCode() { return code; }
    public void setCode(@NonNull String code) { this.code = code; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public double getTargetPercentage() { return targetPercentage; }
    public void setTargetPercentage(double targetPercentage) { this.targetPercentage = targetPercentage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
