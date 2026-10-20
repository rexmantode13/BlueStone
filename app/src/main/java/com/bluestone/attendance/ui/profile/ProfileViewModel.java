package com.bluestone.attendance.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.data.repository.SettingsRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final LiveData<Student> student;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.settingsRepository = new SettingsRepository(application);
        this.student = settingsRepository.getActiveStudent();
    }

    public LiveData<Student> getStudent() {
        return student;
    }

    public void updateProfile(Student student, Runnable onComplete) {
        settingsRepository.updateStudentProfile(student, onComplete);
    }
}
