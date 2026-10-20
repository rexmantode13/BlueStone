package com.bluestone.attendance.data.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "students",
    indices = {@Index(value = {"student_id"}, unique = true)}
)
public class Student {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "student_id")
    private String studentId;

    @NonNull
    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "course")
    private String course;

    @ColumnInfo(name = "semester")
    private int semester;

    @ColumnInfo(name = "academic_year")
    private String academicYear;

    @ColumnInfo(name = "college")
    private String college;

    @NonNull
    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @NonNull
    @ColumnInfo(name = "salt")
    private String salt;

    public Student(@NonNull String studentId, @NonNull String fullName, String email,
                   String course, int semester, String academicYear, String college,
                   @NonNull String passwordHash, @NonNull String salt) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.course = course;
        this.semester = semester;
        this.academicYear = academicYear;
        this.college = college;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String studentId) { this.studentId = studentId; }

    @NonNull public String getFullName() { return fullName; }
    public void setFullName(@NonNull String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    @NonNull public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(@NonNull String passwordHash) { this.passwordHash = passwordHash; }

    @NonNull public String getSalt() { return salt; }
    public void setSalt(@NonNull String salt) { this.salt = salt; }
}
