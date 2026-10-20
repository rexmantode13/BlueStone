package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AttendanceRecord record);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AttendanceRecord> records);

    @Update
    void update(AttendanceRecord record);

    @Delete
    void delete(AttendanceRecord record);

    @Query("DELETE FROM attendance_records WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM attendance_records WHERE id = :id LIMIT 1")
    AttendanceRecord getByIdSync(long id);

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId AND date = :date AND session_number = :sessionNumber LIMIT 1")
    AttendanceRecord findRecordSync(long subjectId, LocalDate date, int sessionNumber);

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId AND date = :date AND session_number = :sessionNumber LIMIT 1")
    LiveData<AttendanceRecord> findRecord(long subjectId, LocalDate date, int sessionNumber);

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, session_number DESC")
    LiveData<List<AttendanceRecord>> getAll();

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, session_number DESC")
    List<AttendanceRecord> getAllSync();

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId ORDER BY date DESC, session_number DESC")
    LiveData<List<AttendanceRecord>> getBySubject(long subjectId);

    @Query("SELECT * FROM attendance_records WHERE subject_id = :subjectId ORDER BY date DESC, session_number DESC")
    List<AttendanceRecord> getBySubjectSync(long subjectId);

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY session_number ASC")
    LiveData<List<AttendanceRecord>> getByDate(LocalDate date);

    @Query("SELECT * FROM attendance_records WHERE date >= :start AND date <= :end ORDER BY date ASC, session_number ASC")
    LiveData<List<AttendanceRecord>> getByDateRange(LocalDate start, LocalDate end);

    @Query("SELECT * FROM attendance_records WHERE date >= :start AND date <= :end ORDER BY date ASC, session_number ASC")
    List<AttendanceRecord> getByDateRangeSync(LocalDate start, LocalDate end);

    @Query("DELETE FROM attendance_records")
    void deleteAll();
}
