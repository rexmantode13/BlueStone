package com.bluestone.attendance.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluestone.attendance.data.repository.AuthRepository;
import com.bluestone.attendance.data.repository.LocalAuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new LocalAuthRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void login(String studentId, String password) {
        if (studentId == null || studentId.trim().isEmpty()) {
            errorMessage.setValue("Student ID is required");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        authRepository.login(studentId.trim(), password.trim(), new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String id) {
                isLoading.setValue(false);
                loginSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
