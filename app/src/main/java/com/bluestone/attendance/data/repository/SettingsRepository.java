package com.bluestone.attendance.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.data.entities.UserSettings;

public class SettingsRepository {

    private static volatile SettingsRepository INSTANCE;
    private final AppDatabase db;
    private final AppExecutor executor;

    public static SettingsRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SettingsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SettingsRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public SettingsRepository(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    public LiveData<UserSettings> getSettings() {
        return db.settingsDao().getSettings();
    }

    public void updateThreshold(double threshold, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.settingsDao().updateThreshold(threshold);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void updateDarkMode(int darkMode, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.settingsDao().updateDarkMode(darkMode);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public void resetDemoData(Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.seedDatabaseSync();
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }

    public LiveData<Student> getActiveStudent() {
        return db.studentDao().getActiveStudent();
    }

    public void updateStudentProfile(Student student, Runnable onComplete) {
        executor.diskIO().execute(() -> {
            db.studentDao().update(student);
            if (onComplete != null) {
                executor.mainThread().execute(onComplete);
            }
        });
    }
}
