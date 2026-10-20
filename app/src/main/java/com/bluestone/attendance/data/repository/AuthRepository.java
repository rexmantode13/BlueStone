package com.bluestone.attendance.data.repository;

public interface AuthRepository {

    interface AuthCallback {
        void onSuccess(String studentId);
        void onError(String message);
    }

    boolean isLoggedIn();

    String getLoggedInStudentId();

    void login(String studentId, String password, AuthCallback callback);

    void logout();
}
