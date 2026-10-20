package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.Student;

@Dao
public interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Student student);

    @Update
    void update(Student student);

    @Query("SELECT * FROM students WHERE student_id = :studentId LIMIT 1")
    Student getByStudentIdSync(String studentId);

    @Query("SELECT * FROM students WHERE student_id = :studentId LIMIT 1")
    LiveData<Student> getByStudentId(String studentId);

    @Query("SELECT * FROM students WHERE student_id = (SELECT active_student_id FROM user_settings WHERE id = 1) LIMIT 1")
    LiveData<Student> getActiveStudent();

    @Query("SELECT * FROM students WHERE student_id = (SELECT active_student_id FROM user_settings WHERE id = 1) LIMIT 1")
    Student getActiveStudentSync();

    @Query("SELECT * FROM students ORDER BY student_id ASC")
    LiveData<java.util.List<Student>> getAllStudents();

    @Query("SELECT * FROM students ORDER BY student_id ASC")
    java.util.List<Student> getAllStudentsSync();

    @Query("DELETE FROM students")
    void deleteAll();
}
