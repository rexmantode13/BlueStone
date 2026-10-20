package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.Subject;

import java.util.List;

@Dao
public interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Subject subject);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Subject> subjects);

    @Update
    void update(Subject subject);

    @Query("SELECT * FROM subjects WHERE is_archived = 0 ORDER BY name ASC")
    LiveData<List<Subject>> getAllActive();

    @Query("SELECT * FROM subjects WHERE is_archived = 0 ORDER BY name ASC")
    List<Subject> getAllActiveSync();

    @Query("SELECT * FROM subjects WHERE is_archived = 0 AND semester = :semester ORDER BY name ASC")
    LiveData<List<Subject>> getBySemester(int semester);

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    LiveData<Subject> getById(long id);

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    Subject getByIdSync(long id);

    @Query("UPDATE subjects SET is_archived = 1 WHERE id = :id")
    void softDelete(long id);

    @Query("DELETE FROM subjects WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM subjects")
    void deleteAll();
}
