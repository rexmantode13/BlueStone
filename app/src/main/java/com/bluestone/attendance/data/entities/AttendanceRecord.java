package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.bluestone.attendance.domain.model.AttendanceStatus;

import java.time.LocalDate;

@Entity(
    tableName = "attendance_records",
    foreignKeys = @ForeignKey(
        entity = Subject.class,
        parentColumns = "id",
        childColumns = "subject_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"subject_id", "date", "session_number"}, unique = true),
        @Index(value = {"date"}),
        @Index(value = {"subject_id"})
    }
)
public class AttendanceRecord {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "subject_id")
    private long subjectId;

    @NonNull
    @ColumnInfo(name = "date")
    private LocalDate date;

    @ColumnInfo(name = "session_number", defaultValue = "1")
    private int sessionNumber;

    @NonNull
    @ColumnInfo(name = "status")
    private AttendanceStatus status;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "is_cancelled", defaultValue = "0")
    private boolean isCancelled;

    public AttendanceRecord(long subjectId, @NonNull LocalDate date, int sessionNumber,
                            @NonNull AttendanceStatus status, String notes, boolean isCancelled) {
        this.subjectId = subjectId;
        this.date = date;
        this.sessionNumber = sessionNumber;
        this.status = status;
        this.notes = notes;
        this.isCancelled = isCancelled;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSubjectId() { return subjectId; }
    public void setSubjectId(long subjectId) { this.subjectId = subjectId; }

    @NonNull public LocalDate getDate() { return date; }
    public void setDate(@NonNull LocalDate date) { this.date = date; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    @NonNull public AttendanceStatus getStatus() { return status; }
    public void setStatus(@NonNull AttendanceStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }
}
