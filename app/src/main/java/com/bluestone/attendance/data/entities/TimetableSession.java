package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalTime;

@Entity(
    tableName = "timetable_sessions",
    foreignKeys = @ForeignKey(
        entity = Subject.class,
        parentColumns = "id",
        childColumns = "subject_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = {"day_of_week"}),
        @Index(value = {"subject_id"})
    }
)
public class TimetableSession {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "subject_id")
    private long subjectId;

    @ColumnInfo(name = "day_of_week")
    private int dayOfWeek; // 1 = Monday ... 7 = Sunday

    @NonNull
    @ColumnInfo(name = "start_time")
    private LocalTime startTime;

    @NonNull
    @ColumnInfo(name = "end_time")
    private LocalTime endTime;

    @ColumnInfo(name = "room")
    private String room;

    @ColumnInfo(name = "faculty")
    private String faculty;

    @ColumnInfo(name = "is_cancelled", defaultValue = "0")
    private boolean isCancelled;

    public TimetableSession(long subjectId, int dayOfWeek, @NonNull LocalTime startTime,
                            @NonNull LocalTime endTime, String room, String faculty,
                            boolean isCancelled) {
        this.subjectId = subjectId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.faculty = faculty;
        this.isCancelled = isCancelled;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSubjectId() { return subjectId; }
    public void setSubjectId(long subjectId) { this.subjectId = subjectId; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    @NonNull public LocalTime getStartTime() { return startTime; }
    public void setStartTime(@NonNull LocalTime startTime) { this.startTime = startTime; }

    @NonNull public LocalTime getEndTime() { return endTime; }
    public void setEndTime(@NonNull LocalTime endTime) { this.endTime = endTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public boolean isCancelled() { return isCancelled; }
    public void setCancelled(boolean cancelled) { isCancelled = cancelled; }
}
