package com.bluestone.attendance.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.bluestone.attendance.data.database.AppDatabase;
import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.utils.PasswordHasher;

public class LocalAuthRepository implements AuthRepository {

    private static final String PREF_NAME = "bluestone_auth_prefs";
    private static final String KEY_LOGGED_IN = "key_logged_in";
    private static final String KEY_STUDENT_ID = "key_student_id";

    private final SharedPreferences prefs;
    private final AppDatabase db;
    private final AppExecutor executor;

    public LocalAuthRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.db = AppDatabase.getInstance(context);
        this.executor = AppExecutor.getInstance();
    }

    @Override
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    @Override
    public String getLoggedInStudentId() {
        return prefs.getString(KEY_STUDENT_ID, null);
    }

    @Override
    public void login(String studentId, String password, AuthCallback callback) {
        executor.diskIO().execute(() -> {
            Student student = db.studentDao().getByStudentIdSync(studentId);

            // If database hasn't finished onCreate seed yet, seed it now
            if (student == null) {
                db.seedDatabaseSync();
                student = db.studentDao().getByStudentIdSync(studentId.trim());
            }

            if (student == null) {
                executor.mainThread().execute(() -> callback.onError("Invalid Student ID or password"));
                return;
            }

            byte[] salt = PasswordHasher.fromHex(student.getSalt());
            byte[] storedHash = PasswordHasher.fromHex(student.getPasswordHash());
            boolean verified = PasswordHasher.verifyPassword(password.toCharArray(), salt, storedHash);

            if (verified) {
                prefs.edit()
                        .putBoolean(KEY_LOGGED_IN, true)
                        .putString(KEY_STUDENT_ID, student.getStudentId())
                        .apply();
                db.settingsDao().updateActiveStudentId(student.getStudentId());
                executor.mainThread().execute(() -> callback.onSuccess(studentId));
            } else {
                executor.mainThread().execute(() -> callback.onError("Invalid Student ID or password"));
            }
        });
    }

    @Override
    public void logout() {
        prefs.edit().clear().apply();
    }
}
