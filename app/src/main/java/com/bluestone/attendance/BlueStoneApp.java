package com.bluestone.attendance;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.repository.AppExecutor;
import com.bluestone.attendance.data.repository.SettingsRepository;

public class BlueStoneApp extends Application {

    private SettingsRepository settingsRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Database & Executor
        AppDatabase db = AppDatabase.getInstance(this);
        settingsRepository = new SettingsRepository(this);

        // Apply dark mode settings
        AppExecutor.getInstance().diskIO().execute(() -> {
            var settings = db.settingsDao().getSettingsSync();
            if (settings != null) {
                int mode = settings.getDarkMode();
                AppExecutor.getInstance().mainThread().execute(() -> {
                    if (mode == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (mode == 2) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                });
            }
        });
    }
}
