package com.bluestone.attendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bluestone.attendance.data.entities.UserSettings;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSettings settings);

    @Update
    void update(UserSettings settings);

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    LiveData<UserSettings> getSettings();

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    UserSettings getSettingsSync();

    @Query("UPDATE user_settings SET attendance_threshold = :threshold WHERE id = 1")
    void updateThreshold(double threshold);

    @Query("UPDATE user_settings SET dark_mode_mode = :darkMode WHERE id = 1")
    void updateDarkMode(int darkMode);

    @Query("UPDATE user_settings SET active_student_id = :studentId WHERE id = 1")
    void updateActiveStudentId(String studentId);
}
