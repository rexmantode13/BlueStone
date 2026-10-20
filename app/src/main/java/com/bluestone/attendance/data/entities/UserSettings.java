package com.bluestone.attendance.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_settings")
public class UserSettings {

    @PrimaryKey
    private int id = 1;

    @ColumnInfo(name = "attendance_threshold", defaultValue = "75.0")
    private double attendanceThreshold;

    @ColumnInfo(name = "dark_mode_mode", defaultValue = "0")
    private int darkMode; // 0 = System, 1 = Light, 2 = Dark

    @ColumnInfo(name = "active_student_id")
    private String activeStudentId;

    public UserSettings(double attendanceThreshold, int darkMode, String activeStudentId) {
        this.attendanceThreshold = attendanceThreshold;
        this.darkMode = darkMode;
        this.activeStudentId = activeStudentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAttendanceThreshold() { return attendanceThreshold; }
    public void setAttendanceThreshold(double attendanceThreshold) { this.attendanceThreshold = attendanceThreshold; }

    public int getDarkMode() { return darkMode; }
    public void setDarkMode(int darkMode) { this.darkMode = darkMode; }

    public String getActiveStudentId() { return activeStudentId; }
    public void setActiveStudentId(String activeStudentId) { this.activeStudentId = activeStudentId; }
}
