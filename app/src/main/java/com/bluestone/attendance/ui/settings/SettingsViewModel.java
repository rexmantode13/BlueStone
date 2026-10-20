package com.bluestone.attendance.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.entities.UserSettings;
import com.bluestone.attendance.data.repository.AuthRepository;
import com.bluestone.attendance.data.repository.LocalAuthRepository;
import com.bluestone.attendance.data.repository.SettingsRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final AuthRepository authRepository;
    private final LiveData<UserSettings> settings;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        this.settingsRepository = new SettingsRepository(application);
        this.authRepository = new LocalAuthRepository(application);
        this.settings = settingsRepository.getSettings();
    }

    public LiveData<UserSettings> getSettings() {
        return settings;
    }

    public void updateThreshold(double threshold) {
        settingsRepository.updateThreshold(threshold, null);
    }

    public void setDarkMode(int mode) {
        settingsRepository.updateDarkMode(mode, () -> {
            if (mode == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (mode == 2) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }

    public void resetDemoData(Runnable onComplete) {
        settingsRepository.resetDemoData(onComplete);
    }

    public void logout() {
        authRepository.logout();
    }
}
