package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.TimetableSession;

import java.time.LocalTime;
import java.util.List;

@Dao
public interface TimetableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TimetableSession session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TimetableSession> sessions);

    @Update
    void update(TimetableSession session);

    @Delete
    void delete(TimetableSession session);

    @Query("DELETE FROM timetable_sessions WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM timetable_sessions WHERE id = :id LIMIT 1")
    TimetableSession getByIdSync(long id);

    @Query("SELECT * FROM timetable_sessions ORDER BY day_of_week ASC, start_time ASC")
    LiveData<List<TimetableSession>> getAll();

    @Query("SELECT * FROM timetable_sessions ORDER BY day_of_week ASC, start_time ASC")
    List<TimetableSession> getAllSync();

    @Query("SELECT * FROM timetable_sessions WHERE day_of_week = :dayOfWeek ORDER BY start_time ASC")
    LiveData<List<TimetableSession>> getByDay(int dayOfWeek);

    @Query("SELECT * FROM timetable_sessions WHERE day_of_week = :dayOfWeek ORDER BY start_time ASC")
    List<TimetableSession> getByDaySync(int dayOfWeek);

    @Query("SELECT * FROM timetable_sessions WHERE day_of_week = :dayOfWeek AND id != :excludeId AND ((start_time < :endTime AND end_time > :startTime))")
    List<TimetableSession> findOverlappingSessionsSync(int dayOfWeek, LocalTime startTime, LocalTime endTime, long excludeId);

    @Query("DELETE FROM timetable_sessions")
    void deleteAll();
}
