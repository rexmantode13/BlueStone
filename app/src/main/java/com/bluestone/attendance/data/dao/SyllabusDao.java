package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.data.entities.Topic;

import java.util.List;

@Dao
public interface SyllabusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUnit(SyllabusUnit unit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUnits(List<SyllabusUnit> units);

    @Update
    void updateUnit(SyllabusUnit unit);

    @Delete
    void deleteUnit(SyllabusUnit unit);

    @Query("DELETE FROM syllabus_units WHERE id = :unitId")
    void deleteUnitById(long unitId);

    @Query("SELECT * FROM syllabus_units WHERE subject_id = :subjectId ORDER BY unit_number ASC")
    LiveData<List<SyllabusUnit>> getUnitsBySubject(long subjectId);

    @Query("SELECT * FROM syllabus_units WHERE subject_id = :subjectId ORDER BY unit_number ASC")
    List<SyllabusUnit> getUnitsBySubjectSync(long subjectId);

    @Query("SELECT * FROM syllabus_units WHERE id = :unitId LIMIT 1")
    SyllabusUnit getUnitByIdSync(long unitId);

    @Query("UPDATE syllabus_units SET is_completed = :isCompleted WHERE id = :unitId")
    void setUnitCompleted(long unitId, boolean isCompleted);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTopic(Topic topic);

    @Update
    void updateTopic(Topic topic);

    @Delete
    void deleteTopic(Topic topic);

    @Query("SELECT * FROM topics WHERE unit_id = :unitId ORDER BY id ASC")
    LiveData<List<Topic>> getTopicsByUnit(long unitId);

    @Query("DELETE FROM syllabus_units")
    void deleteAllUnits();
}
